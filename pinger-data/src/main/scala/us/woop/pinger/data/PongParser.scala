package us.woop.pinger.data

import akka.util.ByteString
import com.typesafe.scalalogging.slf4j.StrictLogging
import us.woop.pinger.data.ParsedPongs._

/** 01/02/14 */
object PongParser extends StrictLogging {

  val matchers: PartialFunction[ByteString, Any] = {
    case GetRelaxedPlayerExtInfo(x) => x
    case GetServerInfoReply(x) => x
    case GetPlayerCns(x) => x
    case GetHopmodUptime(x) => x
    case GetTeamScores(x) => x
    case GetUptime(x) => x
    case GetThomasModExtInfo(x) => x
    case CheckOlderClient(x) => x
  }

  object GetUChar {
    def unapply(byte: Byte): Option[Int] =
      Option(byte.toChar & 0xFF)

    def unapply(int: Int): Option[Int] =
      Option(int & 0xFF)
  }

  object ##:: {
    def unapply(from: ByteString): Option[(Byte, ByteString)] =
      for { ht @ (head, tail) <- Option(from.splitAt(1))
        if head.nonEmpty } yield (head.head, tail)
  }

  object GetInt {
    def unapply(bytes: ByteString): Option[(Int, ByteString)] = bytes match {
      case -127 ##:: GetUChar(m) ##:: GetUChar(n) ##:: GetUChar(o) ##:: GetUChar(p) ##:: rest =>
        Option(((m | (n << 8)) | o << 16) | (p << 24), rest)
      case -128 ##:: GetUChar(m) ##:: n ##:: rest =>
        Option(m | (n << 8), rest)
      case n ##:: rest =>
        Option((n.toInt, rest))
      case ByteString.empty =>
        None
    }
  }

  object GetUchars {

    def uchars(bytes: ByteString): List[(Int, ByteString)] = {
      bytes match {
        case GetInt(GetUChar(value), rest) => (value, rest) :: uchars(rest)
        case ByteString.empty => Nil
      }
    }

    def unapply(bytes: ByteString): Option[List[Int]] =
      Option(uchars(bytes).map(_._1))
  }

  object GetInts {
    def ints(bytes: ByteString): List[(Int, ByteString)] = {
      bytes match {
        case GetInt(value, rest) => (value, rest) :: ints(rest)
        case ByteString.empty => Nil
      }
    }

    def unapply(bytes: ByteString): Option[List[Int]] =
      Option(ints(bytes).map(_._1))
  }

  object CubeString {
    val mapping: PartialFunction[Int, Int] = List[Int](
      0, 192, 193, 194, 195, 196, 197, 198, 199, 9, 10, 11, 12, 13, 200, 201,
      202, 203, 204, 205, 206, 207, 209, 210, 211, 212, 213, 214, 216, 217, 218, 219,
      32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
      48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
      64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
      80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
      96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
      112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 220,
      221, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237,
      238, 239, 241, 242, 243, 244, 245, 246, 248, 249, 250, 251, 252, 253, 255, 0x104,
      0x105, 0x106, 0x107, 0x10C, 0x10D, 0x10E, 0x10F, 0x118, 0x119, 0x11A, 0x11B, 0x11E, 0x11F, 0x130, 0x131, 0x141,
      0x142, 0x143, 0x144, 0x147, 0x148, 0x150, 0x151, 0x152, 0x153, 0x158, 0x159, 0x15A, 0x15B, 0x15E, 0x15F, 0x160,
      0x161, 0x164, 0x165, 0x16E, 0x16F, 0x170, 0x171, 0x178, 0x179, 0x17A, 0x17B, 0x17C, 0x17D, 0x17E, 0x404, 0x411,
      0x413, 0x414, 0x416, 0x417, 0x418, 0x419, 0x41B, 0x41F, 0x423, 0x424, 0x426, 0x427, 0x428, 0x429, 0x42A, 0x42B,
      0x42C, 0x42D, 0x42E, 0x42F, 0x431, 0x432, 0x433, 0x434, 0x436, 0x437, 0x438, 0x439, 0x43A, 0x43B, 0x43C, 0x43D,
      0x43F, 0x442, 0x444, 0x446, 0x447, 0x448, 0x449, 0x44A, 0x44B, 0x44C, 0x44D, 0x44E, 0x44F, 0x454, 0x490, 0x491
    ).orElse {
      case x: Int => x.asInstanceOf[Int]
    }

  }

  object GetString {
    val mapper = (x: (Int, ByteString)) => CubeString.mapping(x._1).toChar

    def unapply(bytes: ByteString): Option[(String, ByteString)] = bytes match {
      case ByteString.empty =>
        None
      case something =>
        val (forStr, rest) = GetUchars.uchars(bytes).span(i => i._1 > 0)
        Option((forStr.map(mapper).mkString, ByteString(rest.take(1).flatMap(_._2).toArray)))
    }
  }

  val >>##:: = GetString

  val >>: = GetInt

  object GetServerInfoReply {
    def unapply(List: ByteString): Option[ServerInfoReply] = List match {
      case 1 >>: 1 >>: 1 >>: clients >>: numattrs >>: protocol >>: gamemode >>:
        remain >>: maxclients >>: pass >>: gamepaused >>:
        gamespeed >>: mapname >>##:: desc >>##:: rest if numattrs == 7 =>
        Option(ServerInfoReply(clients, protocol, gamemode, remain, maxclients, Option(gamepaused), Option(gamespeed), mapname, desc))
      case 1 >>: 1 >>: 1 >>: clients >>: numattrs >>: protocol >>: gamemode >>:
        remain >>: maxclients >>: pass >>: mapname >>##:: desc >>##:: rest if numattrs == 5 =>
        Option(ServerInfoReply(clients, protocol, gamemode, remain, maxclients, None, None, mapname, desc))
      case _ => None
    }
  }

  val ack = -1
  object GetHopmodUptime {
    def unapply(List: ByteString): Option[HopmodUptime] = List match {
      case 0 >>: 0 >>: -1 >>: `ack` >>: version >>: totalsecs >>: isHopmod >>: hopmodVersion >>: hopmodRevision >>: buildTime >>##:: ByteString.empty =>
        Option(HopmodUptime(Uptime(version, totalsecs), hopmodVersion, hopmodRevision, buildTime))
      case _ => None
    }
  }
  object GetUptime {
    def unapply(List: ByteString): Option[Uptime] = List match {
      case 0 >>: 0 >>: -1 >>: `ack` >>: version >>: totalsecs >>: ByteString.empty =>
        Option(Uptime(version, totalsecs))
      case _ => None
    }
  }
  object GetPlayerCns {
    def unapply(List: ByteString): Option[PlayerCns] = List match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -10 >>: GetInts(ids) =>
        Option(PlayerCns(version, ids.toList))
      case _ => None
    }
  }

  object GetIp {
    def unapply(List: ByteString): Option[(String, ByteString)] = List match {
      case GetUChar(a) ##:: GetUChar(b) ##:: GetUChar(c) ##:: rest =>
        Option(s"$a.$b.$c.x", rest)
      case _ =>
        None
    }
  }
  val >~: = GetIp
  object GetThomasModExtInfo {
    def unapply(list: ByteString): Option[PartialPlayerExtInfo] = list match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: (rest @ (-3 >>: _)) =>
        val ititi = Iterator.iterate(Option(rest)) {
          case Some(-3 >>: _ >>: _ >>: _ >>: _ >>: _ >>: _ >>: _ >>: other) =>
            Some(other)
          case _ => None
        }.takeWhile(_.nonEmpty).toList.flatten.lastOption
        ititi.flatMap {
          case name >>##:: team >>##:: frags >>: flags >>: deaths >>:
            teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
            >>: ip >~: ByteString.empty =>
            Option(PartialPlayerExtInfo(PlayerExtInfo(version, -1, -1, name, team, frags, deaths, teamkills, accuracy, health, armour, gun, privilege, state, ip)))
//          case name >>##:: team >>##:: frags >>: other =>
//            Option(PartialPlayerExtInfo(PlayerExtInfo(version, -1, -1, name, team, frags, -1, -1, -1, -1, -1, -1, -1, -1, ".")))
          case c =>
//            println(s"Failed here ==> $c");
            None
        }
      case x =>
        None
    }
  }

  object CheckOlderClient {
    def unapply(list: ByteString): Option[OlderClient] = list match {
      case 0 >>: _ >>: -1 >>: -1 >>: 105 >>: ByteString.empty =>
        Option(OlderClient())
      case _ => None
    }
  }


  object GetPlayerExtInfo {
    def unapply(list: ByteString): Option[PlayerExtInfo] = list match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>##:: team >>##:: frags >>: flags >>: deaths >>:
        teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
        >>: ip >~: ByteString.empty =>
        Option(PlayerExtInfo(version, cn, ping, name, team, frags, deaths, teamkills, accuracy, health, armour,
          gun, privilege, state, ip))
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>##:: team >>##:: rest => None
      case _ => None
    }
  }

  object GetRelaxedPlayerExtInfo {
    def unapply(list: ByteString): Option[PlayerExtInfo] = list match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>##:: team >>##:: frags >>: flags >>: deaths >>:
        teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
        >>: ip >~: _ =>
        val vinfo = PlayerExtInfo(version, cn, ping, name, team, frags, deaths, teamkills, accuracy, health, armour,
          gun, privilege, state, ip)
        Option(vinfo)
      case 0 >>: 1 >>: -1 >>: `ack` >>: 105 >>: 0 >>: -10 >>: _ =>
        None
      case 0 >>: 1 >>: -1 >>: `ack` >>: other =>
        None
      case _ => None
    }
  }

  object GetTeamScores {
    def unapply(List: ByteString): Option[TeamScores] = List match {
      case 0 >>: 2 >>: -1 >>: `ack` >>: version >>: 1 >>: gamemode >>: remain >>: ByteString.empty =>
        Option(TeamScores(version, gamemode, remain, Nil))
      case 0 >>: 2 >>: -1 >>: `ack` >>: version >>: 0 >>: gamemode >>: remain >>: scores =>
        val ret = GetTeamScore.many(scores)
        val ascores = ret.map(_._1)
        Option(TeamScores(version, gamemode, remain, ascores.toList))
      case _ =>
        None
    }
  }

  object GetTeamScore {
    def ints(bytes: ByteString): List[(Int, ByteString)] = {
      bytes match {
        case GetInt(value, rest) => (value, rest) :: ints(rest)
        case ByteString.empty => Nil
      }
    }

    def unapply(List: ByteString): Option[(TeamScore, ByteString)] = List match {
      case name >>##:: score >>: -1 >>: rest =>
        Option(TeamScore(name, score, baseMap = false, Nil), rest)
      case name >>##:: score >>: numBases >>: rest =>
        val collected = GetInts.ints(rest).take(numBases)
        val baseScores = collected.map(_._1)
        val leftOvers = collected.lastOption.map(_._2).getOrElse(ByteString.empty)
        Option((TeamScore(name, score, baseMap = true, baseScores.toList), leftOvers))
      case _ =>
        None
    }

    def many(bytes: ByteString): List[(TeamScore, ByteString)] = {
      bytes match {
        case GetTeamScore(teamScore, rest) => (teamScore, rest) :: many(rest)
        case _ => Nil
      }
    }
  }

}
