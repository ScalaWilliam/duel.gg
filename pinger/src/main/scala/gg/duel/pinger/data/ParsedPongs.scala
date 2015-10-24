package gg.duel.pinger.data

import org.joda.time.format.ISODateTimeFormat
import gg.duel.pinger.data.ParsedPongs.ConvertedMessages.{ConvertedHopmodUptime, ConvertedServerInfoReply, ConvertedTeamScore, ConvertedThomasExt}

object ParsedPongs {

  case class ParsedMessage(time: Long, message: Any) {
    def stringTime = ISODateTimeFormat.dateTimeNoMillis().print(time)
    override def toString = s"ParsedMessage($stringTime, $message)"
  }

  case class ServerInfoReply(clients: Int, protocol: Int, gamemode: Int, remain: Int, maxclients: Int,
                             gamepaused: Option[Int], gamespeed: Option[Int], mapname: String, description: String)
  case class Uptime(version: Int, totalsecs: Int)
  case class HopmodUptime(uptime: Uptime, hopmodVersion: Int, hopmodRevision: Int, buildTime: String)
  case class PlayerCns(version: Int, cns: List[Int])
  case class PlayerExtInfo(version: Int, cn: Int, ping: Int, name: String, team: String, frags: Int,
                           deaths: Int, teamkills: Int, accuracy: Int, health: Int, armour: Int, gun: Int, privilege: Int, state: Int, ip: String) {
    override def toString = s"[$ip] name: $name, version: $version, cn: $cn, ping: $ping" +
      s", name: $name, team: $team, frags: $frags, deaths: $deaths, teamkills: $teamkills, accuracy: $accuracy,"+
      s"health: $health, armour: $armour, gun: $gun, privilege: $privilege, state: $state, ip: $ip"
  }
  case class ThomasExt(ds: List[ThomasD], r: ThomasR)
  case class ThomasD(data: List[Int])

  case class PartialPlayerExtInfo(playerExtInfo: PlayerExtInfo)
  case class ThomasR(s1: Option[String], s2: String, data: List[Int])
  case class OlderClient()
  case class TeamScores(version: Int, gamemode: Int, remain: Int, scores: List[TeamScore])
  case class TeamScore(name: String, score: Int, baseMap: Boolean, baseScores: List[Int])
  object ConvertedMessages {

    case class ConvertedThomasExt(i: Int, dataA: List[Int], dataB: List[Int], s1: String, s2: String)
    case class ConvertedHopmodUptime(version: Int, totalsecs: Int, hopmodVersion: Int, hopmodRevision: Int, buildTime: String)
    case class ConvertedServerInfoReply(clients: Int, protocol: Int, gamemode: Int, remain: Int, maxclients: Int,
                                        gamepaused: Boolean, gamespeed: Int, mapname: String, description: String)

    case class ConvertedTeamScore(scoreNum: Int, version: Int, gamemode: Int, remain: Int, name: String, score: Int, baseMap: Boolean, baseScores: List[Int])

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

    object ConvertServerInfoReply {
      def convert (obj: ServerInfoReply): ConvertedServerInfoReply = {
        import obj._
        ConvertedServerInfoReply(
          clients = clients, protocol = protocol, gamemode = gamemode,
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
