package us.woop.pinger.data

import us.woop.pinger.data.actor.PingPongProcessor.Server
import us.woop.pinger.data.ParsedPongs.ConvertedMessages.{ConvertedThomasExt, ConvertedTeamScore, ConvertedHopmodUptime, ConvertedServerInfoReply}

object ParsedPongs {

  case class ParsedMessage(server: Server, time: Long, message: Any)

  case class ServerInfoReply(clients: Int, protocol: Int, gamemode: Int, remain: Int, maxclients: Int,
                             gamepaused: Option[Int], gamespeed: Option[Int], mapname: String, description: String)
  case class Uptime(version: Int, totalsecs: Int)
  case class HopmodUptime(uptime: Uptime, hopmodVersion: Int, hopmodRevision: Int, buildTime: String)
  case class PlayerCns(version: Int, cns: List[Int])
  case class PlayerExtInfo(version: Int, cn: Int, ping: Int, name: String, team: String, frags: Int,
                           deaths: Int, teamkills: Int, accuracy: Int, health: Int, armour: Int, gun: Int, privilege: Int, state: Int, ip: String)
  case class ThomasExt(ds: List[ThomasD], r: ThomasR)
  case class ThomasD(data: List[Int])
  case class ThomasR(s1: Option[String], s2: String, data: List[Int])
  case class OlderClient()
  case class TeamScores(version: Int, gamemode: Int, remain: Int, scores: List[TeamScore])
  case class TeamScore(name: String, score: Int, baseMap: Boolean, baseScores: List[Int])
  case class Gamemode(id: Int)
  object ConvertedMessages {

    case class ConvertedThomasExt(i: Int, dataA: List[Int], dataB: List[Int], s1: String, s2: String)
    case class ConvertedHopmodUptime(version: Int, totalsecs: Int, hopmodVersion: Int, hopmodRevision: Int, buildTime: String)
    case class ConvertedServerInfoReply(clients: Int, protocol: Int, gamemode: Option[Gamemode], remain: Int, maxclients: Int,
                                        gamepaused: Boolean, gamespeed: Int, mapname: String, description: String)

    case class ConvertedTeamScore(scoreNum: Int, version: Int, gamemode: Int, remain: Int, name: String, score: Int, baseMap: Boolean, baseScores: List[Int])

  }
  object TypedMessages {

    case class ParsedTypedMessage[T](server: Server, time: Long, message: T)

    import scala.reflect.runtime.universe._
    abstract class ParsedTypedMessageConversion[T](implicit tt: TypeTag[T]) {
      def getType(message: Any) = runtimeMirror(getClass.getClassLoader).classSymbol(message.getClass).toType
      def unapply(m: ParsedMessage): Option[ParsedTypedMessage[T]] = {
        Option(m).collect {
          case ParsedMessage(server, time, message) if getType(message) =:= typeOf[T] =>
            ParsedTypedMessage(server, time, message.asInstanceOf[T])
        }
      }
    }


    object ParsedTypedMessages {
      object ParsedTypedMessageConvertedServerInfoReply extends ParsedTypedMessageConversion[ConvertedServerInfoReply]
      object ParsedTypedMessageConvertedHopmodUptime extends ParsedTypedMessageConversion[ConvertedHopmodUptime]
      object ParsedTypedMessageConvertedTeamScore extends ParsedTypedMessageConversion[ConvertedTeamScore]
      object ParsedTypedMessageConvertedThomasExt extends ParsedTypedMessageConversion[ConvertedThomasExt]
      object ParsedTypedMessageServerInfoReply extends ParsedTypedMessageConversion[ServerInfoReply]
      object ParsedTypedMessageHopmodUptime extends ParsedTypedMessageConversion[HopmodUptime]
      object ParsedTypedMessageOlderClient extends ParsedTypedMessageConversion[OlderClient]
      object ParsedTypedMessagePlayerCns extends ParsedTypedMessageConversion[PlayerCns]
      object ParsedTypedMessagePlayerExtInfo extends ParsedTypedMessageConversion[PlayerExtInfo]
      object ParsedTypedMessageTeamScores extends ParsedTypedMessageConversion[TeamScores]
      object ParsedTypedMessageThomasExt extends ParsedTypedMessageConversion[ThomasExt]
      object ParsedTypedMessageUptime extends ParsedTypedMessageConversion[Uptime]
    }
  }

  object Conversions {

    object ConvertHopmodUptime {
      def convert(obj: HopmodUptime): ConvertedHopmodUptime = {
        import obj._
        import uptime._
        ConvertedHopmodUptime(
          version = version, totalsecs = totalsecs,
          hopmodVersion = hopmodVersion,
          hopmodRevision = hopmodRevision, buildTime = buildTime
        )
      }
    }

    val validGamemodeIs = (0 to 22).toList

    object ConvertServerInfoReply {
      def convert (obj: ServerInfoReply): ConvertedServerInfoReply = {
        import obj._
        ConvertedServerInfoReply(
          clients = clients, protocol = protocol, gamemode = validGamemodeIs.lift.apply(gamemode).map(Gamemode),
          remain = remain, maxclients = maxclients, gamepaused = gamepaused.getOrElse(0) != 0,
          gamespeed = gamespeed.getOrElse(100), mapname = mapname, description = description
        )
      }
    }

    object ConvertThomasExt {
      def convert(ext: ThomasExt): Seq[ConvertedThomasExt] = {
        val converted: Seq[ConvertedThomasExt] = for {
          ThomasExt(thomasDs, ThomasR(s1, s2, dataB)) <- ext :: Nil
          ThomasD(dataA) <- thomasDs
        } yield ConvertedThomasExt(0, dataA, dataB, s1.getOrElse(""), s2)
        converted.zipWithIndex.map{case(e, i) => e.copy(i = i)}
      }
    }

    object ConvertTeamScore {
      def convert(ts: TeamScores): Seq[ConvertedTeamScore] = {
        val converted: Seq[ConvertedTeamScore] = for {
          TeamScores(version, gamemode, remain, scores) <- ts :: Nil
          TeamScore(name, score, baseMap, baseScores) <- scores
        } yield ConvertedTeamScore(0, version, gamemode, remain, name, score, baseMap, baseScores)
        converted.zipWithIndex.map{case(e,i)=>e.copy(scoreNum = i)}
      }
    }

  }

}
