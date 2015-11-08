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

  def server = Option(gameNode.get("server")).map(_.textValue())
  def map = Option(gameNode.get("map")).map(_.textValue())
  def tags = gameNode.getArrayL("tags").collect{ case s: TextNode => s.textValue() }.toSet
  def demo = Option(gameNode.get("demo")).map(_.textValue())
  def mode = Option(gameNode.get("mode")).map(_.textValue())

  sealed abstract class AnyPlayerNode(playerNode: ObjectNode) {
    def name = Option(playerNode.get("name")).map(_.textValue())
    def ip = Option(playerNode.get("ip")).map(_.textValue())
    def clan = Option(playerNode.get("clan")).map(_.textValue())

    /** MUTATIONS **/
    def attachClan(): Unit = {
      name.flatMap(plainGameEnricher.lookupClan).foreach{
        clanName => playerNode.put("clan", clanName) }
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
  def startTime = Option(gameNode.get("startTimeText")).map(stt => ZonedDateTime.parse(stt.textValue()))
  def startTimeText = Option(gameNode.get("startTimeText")).map(_.textValue())
  def duration = Option(gameNode.get("duration")).map(_.intValue())
  case class PlayerNode(playerNode: ObjectNode) extends AnyPlayerNode(playerNode) {
    def fragLog = playerNode.getArrayO("fragLog").map(SomeLog)
  }
  case class TeamNode(teamNode: ObjectNode) {
    def name = Option(teamNode.get("name")).map(_.textValue())
    def players = teamNode.safeGetArrayObjects("players").map(TeamPlayerNode)
    def flagLog = teamNode.getArrayO("flagLog").map(SomeLog)
    case class TeamPlayerNode(teamPlayerNode: ObjectNode) extends AnyPlayerNode(teamPlayerNode)
    def clan = Option(teamNode.get("clan")).map(_.textValue())

    /** MUTATIONS **/
    def attachClan(): Unit = {
      if ( players.size >= 2 ) {
        val clanOptions = players.map(_.clan).toSet
        if ( clanOptions.size == 1 ) {
          clanOptions.flatten.foreach(clanName => teamNode.put("clan", clanName))
        }
      }
    }
  }

  def asJson = om.writeValueAsString(gameNode)
  def asPrettyJson = om.writerWithDefaultPrettyPrinter[ObjectWriter]().writeValueAsString(gameNode)

  def isClanWar = teams.nonEmpty && teams.flatMap(_.clan).toSet.size == 2

  def gameType = Option(gameNode.get("type")).map(_.textValue())

  /** MUTATIONS **/

  def transformLogs(): Unit = {
    players.flatMap(_.fragLog).foreach(_.transform())
    teams.flatMap(_.flagLog).foreach(_.transform())
  }

  def addGameType(): Unit = {
    gameNode.put("type", if ( teams.nonEmpty ) "ctf" else "duel")
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
    if ( isClanWar ) newArray.add("clanwar")
    gameType.foreach(gt => newArray.add(gt))
  }

  def addEndTime(): Unit = {
    for {
      startTime <- startTime
      duration <- duration
    } gameNode.put("endTime", startTime.plusMinutes(duration).toString())
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
  }

}
