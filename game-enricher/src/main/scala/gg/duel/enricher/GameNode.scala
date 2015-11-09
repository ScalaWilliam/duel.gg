package gg.duel.enricher

import java.time.ZonedDateTime

import com.fasterxml.jackson.databind.{ObjectWriter, ObjectMapper}
import com.fasterxml.jackson.databind.node.{TextNode, ObjectNode}
import gg.duel.pinger.analytics.duel.StubGenerator
import gg.duel.enricher.lookup.LookingUp

/**
  * Created by William on 08/11/2015.
  */

object GameNode {
  def stubSource = StubGenerator.validSequenceCompletedDuel.toJson
  lazy val om = new ObjectMapper()

  def apply(jsonString: String, plainGameEnricher: LookingUp): GameNode = {
    val rootNode = om.readTree(jsonString)
    GameNode(
      om = om,
      gameNode = rootNode.asInstanceOf[ObjectNode],
      plainGameEnricher = plainGameEnricher
    )
  }
}

case class GameNode(om: ObjectMapper, gameNode: ObjectNode, plainGameEnricher: LookingUp) {
  gameNodeV =>


  def server = gameNode.getStringO("server")

  def map = gameNode.getStringO("map")

  def tags = gameNode.getArrayL("tags").collect { case s: TextNode => s.textValue() }.toSet

  def demo = gameNode.getStringO("demo")

  def mode = gameNode.getStringO("mode")

  sealed abstract class AnyPlayerNode(playerNode: ObjectNode) {
    def name = playerNode.getStringO("name")

    def ip = playerNode.getStringO("ip")

    def clan = playerNode.getStringO("clan")

    /** MUTATIONS **/
    object Mutations {
      def attachClan(): Unit = {
        name.flatMap(plainGameEnricher.lookupClan).foreach {
          clanName => playerNode.put("clan", clanName)
        }
      }

      def attachGeo(): Unit = {
        ip.map(_.replaceAllLiterally("x", "1")).flatMap(plainGameEnricher.lookupCountryCodeAndName)
          .foreach { case (code, name) =>
            playerNode.put("countryCode", code)
            playerNode.put("countryName", name)
          }
      }
    }
  }

  def teams = gameNode.safeGetObjectValueObjects("teams").map(TeamNode)

  def players = gameNode.safeGetArrayObjects("players").map(PlayerNode)

  def allPlayers: List[AnyPlayerNode] = players ++ teams.flatMap(_.players)

  def startTime = gameNode.getStringO("startTimeText").map(stt => ZonedDateTime.parse(stt))

  def startTimeText = gameNode.getStringO("startTimeText")

  def duration = gameNode.getIntO("duration")

  case class PlayerNode(playerNode: ObjectNode) extends AnyPlayerNode(playerNode) {
    def fragLog = playerNode.getArrayO("fragLog").map(SomeLog)
  }

  case class TeamNode(teamNode: ObjectNode) {
    def name = teamNode.getStringO("name")

    def players = teamNode.safeGetArrayObjects("players").map(TeamPlayerNode)

    def flagLog = teamNode.getArrayO("flagLog").map(SomeLog)

    case class TeamPlayerNode(teamPlayerNode: ObjectNode) extends AnyPlayerNode(teamPlayerNode)

    def clan = teamNode.getStringO("clan")

    /** MUTATIONS **/
    object Mutations {
      def attachClan(): Unit = {
        if (players.size >= 2) {
          val clanOptions = players.map(_.clan).toSet
          if (clanOptions.size == 1) {
            clanOptions.flatten.foreach(clanName => teamNode.put("clan", clanName))
          }
        }
      }
    }
  }

  def asJson = om.writeValueAsString(gameNode)

  def asPrettyJson = om.writerWithDefaultPrettyPrinter[ObjectWriter]().writeValueAsString(gameNode)

  def isClanWar = teams.nonEmpty && teams.flatMap(_.clan).toSet.size == 2

  def gameType = gameNode.getStringO("type")

  /** MUTATIONS **/
  object Mutations {

    private def transformPlayersIndex(): Unit = {
      val players = gameNode.safeGetObjectValueObjects("players").map(PlayerNode)
      if ( players.nonEmpty ) {
        val playersArray = gameNode.putArray("players")
        players.foreach(player => playersArray.add(player.playerNode))
      }
    }

    private def transformLogs(): Unit = {
      players.flatMap(_.fragLog).foreach(_.transform())
      teams.flatMap(_.flagLog).foreach(_.transform())
    }

    private def addGameType(): Unit = {
      gameNode.put("type", if (teams.nonEmpty) "ctf" else "duel")
    }

    private def removeUnnecessaryNodes(): Unit = {
      gameNode.remove("simpleId")
      gameNode.remove("startTime")
    }

    private def attachDemo(): Unit = {
      plainGameEnricher.lookupDemo(gameNodeV).foreach(demoUrl =>
        gameNode.put("demo", demoUrl)
      )
    }

    private def attachTags(): Unit = {
      val newArray = gameNode.putArray("tags")
      if (isClanWar) newArray.add("clanwar")
      gameType.foreach(gt => newArray.add(gt))
    }

    private def addEndTime(): Unit = {
      for {
        startTime <- startTime
        duration <- duration
      } gameNode.put("endTime", startTime.plusMinutes(duration).toString())
    }

    private def addStartTime(): Unit = {
      startTime.foreach { st =>
        gameNode.put("startTime", s"$st")
      }
    }

    def enrich(): Unit = {
      transformPlayersIndex()
      transformLogs()
      addGameType()
      removeUnnecessaryNodes()
      addEndTime()
      allPlayers.foreach(_.Mutations.attachClan())
      allPlayers.foreach(_.Mutations.attachGeo())
      teams.foreach(_.Mutations.attachClan())
      attachDemo()
      attachTags()
      addStartTime()
    }
  }

}
