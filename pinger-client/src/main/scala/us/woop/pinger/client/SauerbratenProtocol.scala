package us.woop.pinger.client

import com.typesafe.scalalogging.slf4j.Logging
import us.woop.pinger.SauerbratenServerData._

/** 01/02/14 */
object SauerbratenProtocol extends Logging {

  val matchers: PartialFunction[List[_], Any] = {
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

  object GetInt {
    def unapply(bytes: List[_]): Option[(Int, List[Byte])] = bytes.asInstanceOf[List[Byte]] match {
      case -127 :: GetUChar(m) :: GetUChar(n) :: GetUChar(o) :: GetUChar(p) :: rest =>
        Option(((m | (n << 8)) | o << 16) | (p << 24), rest)
      case -128 :: GetUChar(m) :: n :: rest =>
        Option(m | (n << 8), rest)
      case n :: rest =>
        Option((n, rest))
      case Nil =>
        None
    }
  }

  object GetUchars {

    def uchars(bytes: List[_]): List[(Int, List[Byte])] = {
      bytes match {
        case GetInt(GetUChar(value), rest) => (value, rest) :: uchars(rest)
        case Nil => Nil
      }
    }

    def unapply(bytes: List[_]): Option[List[Int]] =
      Option(uchars(bytes).map(_._1))
  }

  object GetInts {
    def ints(bytes: List[_]): List[(Int, List[Byte])] = {
      bytes match {
        case GetInt(value, rest) => (value, rest) :: ints(rest)
        case Nil => Nil
      }
    }

    def unapply(bytes: List[_]): Option[List[Int]] =
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
      case x => x
    }

  }

  object GetString {
    val mapper = (x: (Int, List[Byte])) => CubeString.mapping(x._1).toChar

    def unapply(bytes: List[_]): Option[(String, List[Byte])] = bytes match {
      case Nil =>
        None
      case something =>
        val (forStr, rest) = GetUchars.uchars(bytes).span(i => i._1 > 0)
        Option(forStr.map(mapper).mkString, rest.take(1).flatMap(_._2))
    }
  }

  val >>:: = GetString

  val >>: = GetInt

  object GetServerInfoReply {
    def unapply(List: List[_]): Option[ServerInfoReply] = List match {
      case 1 >>: 1 >>: 1 >>: clients >>: numattrs >>: protocol >>: gamemode >>:
        remain >>: maxclients >>: pass >>: gamepaused >>:
        gamespeed >>: mapname >>:: desc >>:: rest if numattrs == 7 =>
        Option(ServerInfoReply(clients, protocol, gamemode, remain, maxclients, Option(gamepaused), Option(gamespeed), mapname, desc))
      case 1 >>: 1 >>: 1 >>: clients >>: numattrs >>: protocol >>: gamemode >>:
        remain >>: maxclients >>: pass >>: mapname >>:: desc >>:: rest if numattrs == 5 =>
        Option(ServerInfoReply(clients, protocol, gamemode, remain, maxclients, None, None, mapname, desc))
      case _ => None
    }
  }

  val ack = -1
  object GetHopmodUptime {
    def unapply(List: List[_]): Option[HopmodUptime] = List match {
      case 0 >>: 0 >>: -1 >>: `ack` >>: version >>: totalsecs >>: isHopmod >>: hopmodVersion >>: hopmodRevision >>: buildTime >>:: Nil =>
        Option(HopmodUptime(Uptime(version, totalsecs), hopmodVersion, hopmodRevision, buildTime))
      case _ => None
    }
  }
  object GetUptime {
    def unapply(List: List[Byte]): Option[Uptime] = List match {
      case 0 >>: 0 >>: -1 >>: `ack` >>: version >>: totalsecs >>: Nil =>
        Option(Uptime(version, totalsecs))
      case _ => None
    }
  }
  object GetPlayerCns {
    def unapply(List: List[Byte]): Option[PlayerCns] = List match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -10 >>: GetInts(ids) =>
        Option(PlayerCns(version, ids.toList))
      case _ => None
    }
  }

  object GetIp {
    def unapply(List: List[Byte]): Option[(String, List[Byte])] = List match {
      case GetUChar(a) :: GetUChar(b) :: GetUChar(c) :: rest =>
        Option(s"$a.$b.$c.x", rest)
      case _ =>
        None
    }
  }
  val >~: = GetIp
  object GetThomasModExtInfo {
    def unapply(list: List[Byte]): Option[ThomasExt] = list match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: (rest @ (-3 >>: _)) =>
        rest match {
          case GetThomasExt(thomasR) =>
            Option(thomasR)
          case x =>
            None
        }
      case x =>
        None
    }
  }

  object GetThomasExt {

    def getDs(bytes: List[Byte]): List[(ThomasD, List[Byte])] = {
      bytes match {
        case GetD(value, rest) => (value, rest) :: getDs(rest)
        case _ => Nil
      }
    }
    def unapply(list: List[Byte]): Option[ThomasExt] = list match {
      case stuff =>
        val gd = getDs(stuff)
        val allDs = gd.map(_._1)
        val leftOver = gd.last._2
        val (thomasR, lefties) = GetR.unapply(leftOver).get
        Option(ThomasExt(allDs, thomasR))
    }
  }
  object GetD {
    def unapply(list: List[Byte]): Option[(ThomasD, List[Byte])] = list match {
      case -3 >>: rest =>
        val ints = GetInts.ints(rest).take(7)
        val listOfInts = ints.map(_._1)
        val leftOver = ints.last._2
        Option(ThomasD(listOfInts), leftOver)
      case _ => None
    }
  }

  object GetR {
    def unapply(list: List[Byte]): Option[(ThomasR, List[Byte])] = list match {
      case s1 >>:: s2 >>:: rest =>
        val ints = GetInts.ints(rest).take(13)
        val listOfInts = ints.map(_._1)
        val leftOver = ints.last._2
        Option(ThomasR(Option(s1), s2, listOfInts), leftOver)
      case s >>:: rest =>
        val ints = GetInts.ints(rest).take(13)
        val listOfInts = ints.map(_._1)
        val leftOver = ints.last._2
        Option(ThomasR(None, s, listOfInts), leftOver)
      case x =>
        // unknown protocol
        None
    }
  }

  object CheckOlderClient {
    def unapply(list: List[Byte]): Option[OlderClient] = list match {
      case 0 >>: _ >>: -1 >>: -1 >>: 105 >>: Nil =>
        Option(OlderClient())
      case _ => None
    }
  }


  object GetPlayerExtInfo {
    def unapply(list: List[Byte]): Option[PlayerExtInfo] = list match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>:: team >>:: frags >>: flags >>: deaths >>:
        teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
        >>: ip >~: Nil =>
        Option(PlayerExtInfo(version, cn, ping, name, team, frags, deaths, teamkills, accuracy, health, armour,
          gun, privilege, state, ip))
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>:: team >>:: rest =>
        //      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        //        cn >>: ping >>: name >>:: team >>:: frags >>: flags >>: deaths >>:
        //        teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
        //        >>: ip >~: rest =>
        None
      case _ => None
    }
  }

  object GetRelaxedPlayerExtInfo {
    def unapply(list: List[Byte]): Option[PlayerExtInfo] = list match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>:: team >>:: frags >>: flags >>: deaths >>:
        teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
        >>: ip >~: _ =>
        Option(PlayerExtInfo(version, cn, ping, name, team, frags, deaths, teamkills, accuracy, health, armour,
          gun, privilege, state, ip))
      case _ => None
    }
  }

  object GetTeamScores {
    def unapply(List: List[Byte]): Option[TeamScores] = List match {
      case 0 >>: 2 >>: -1 >>: `ack` >>: version >>: 1 >>: gamemode >>: remain >>: Nil =>
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
    def ints(bytes: List[Byte]): List[(Int, List[Byte])] = {
      bytes match {
        case GetInt(value, rest) => (value, rest) :: ints(rest)
        case Nil => Nil
      }
    }

    def unapply(List: List[Byte]): Option[(TeamScore, List[Byte])] = List match {
      case name >>:: score >>: -1 >>: rest =>
        Option(TeamScore(name, score, baseMap = false, Nil), rest)
      case name >>:: score >>: numBases >>: rest =>
        val collected = GetInts.ints(rest).take(numBases)
        val baseScores = collected.map(_._1)
        val leftOvers = collected.lastOption.map(_._2).getOrElse(Nil)
        Option(TeamScore(name, score, baseMap = true, baseScores.toList), leftOvers)
      case _ =>
        None
    }

    def many(bytes: List[Byte]): List[(TeamScore, List[Byte])] = {
      bytes match {
        case GetTeamScore(teamScore, rest) => (teamScore, rest) :: many(rest)
        case Nil => Nil
        //        case other =>
        //          println(s"[WARNING] Unknown teamScore data: $other. Ignoring it.")
        //          Nil
      }
    }
  }

}
