package gg.duel.transformer

import java.time.ZonedDateTime

import com.fasterxml.jackson.databind.{ObjectWriter, ObjectMapper}
import com.fasterxml.jackson.databind.node.{TextNode, ObjectNode}
import gg.duel.pinger.analytics.duel.StubGenerator
import gg.duel.transformer.lookup.LookingUp

/**
  * Created by William on 08/11/2015.
  */

object GameNode {
  def stubSource = StubGenerator.validSequenceCompletedDuel.toJson

  def apply(jsonString: String, plainGameEnricher: LookingUp): GameNode = {
    val om = new ObjectMapper()
    val rootNode = om.readTree(jsonString)
    GameNode(
      om = om,
      gameNode = rootNode.asInstanceOf[ObjectNode],
      plainGameEnricher = plainGameEnricher
    )
  }
}

case class GameNode(om: ObjectMapper, gameNode: ObjectNode, plainGameEnricher: LookingUp) {
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

  def teams = gameNode.safeGetObjectValueObjects("teams").map(TeamNode)

  def players = gameNode.safeGetObjectValueObjects("players").map(PlayerNode)

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
    def attachClan(): Unit = {
      if (players.size >= 2) {
        val clanOptions = players.map(_.clan).toSet
        if (clanOptions.size == 1) {
          clanOptions.flatten.foreach(clanName => teamNode.put("clan", clanName))
        }
      }
    }
  }

  def asJson = om.writeValueAsString(gameNode)

  def asPrettyJson = om.writerWithDefaultPrettyPrinter[ObjectWriter]().writeValueAsString(gameNode)

  def isClanWar = teams.nonEmpty && teams.flatMap(_.clan).toSet.size == 2

  def gameType = gameNode.getStringO("type")

  /** MUTATIONS **/

  def transformLogs(): Unit = {
    players.flatMap(_.fragLog).foreach(_.transform())
    teams.flatMap(_.flagLog).foreach(_.transform())
  }

  def addGameType(): Unit = {
    gameNode.put("type", if (teams.nonEmpty) "ctf" else "duel")
  }

  def removeUnnecessaryNodes(): Unit = {
    gameNode.remove("simpleId")
    gameNode.remove("startTime")
  }

  def attachDemo(): Unit = {
    plainGameEnricher.lookupDemo(this).foreach(demoUrl =>
      gameNode.put("demo", demoUrl)
    )
  }

  def attachTags(): Unit = {
    val newArray = gameNode.putArray("tags")
    if (isClanWar) newArray.add("clanwar")
    gameType.foreach(gt => newArray.add(gt))
  }

  def addEndTime(): Unit = {
    for {
      startTime <- startTime
      duration <- duration
    } gameNode.put("endTime", startTime.plusMinutes(duration).toString())
  }

  def addStartTime(): Unit = {
    startTime.foreach { st =>
      gameNode.put("startTime", s"$st")
    }
  }

  def enrich(): Unit = {
    transformLogs()
    addGameType()
    removeUnnecessaryNodes()
    addEndTime()
    allPlayers.foreach(_.attachClan())
    allPlayers.foreach(_.attachGeo())
    teams.foreach(_.attachClan())
    attachDemo()
    attachTags()
    addStartTime()
  }

}
