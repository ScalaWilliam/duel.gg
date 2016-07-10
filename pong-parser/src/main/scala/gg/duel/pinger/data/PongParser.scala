package gg.duel.pinger.data

import java.util.function.Supplier

import akka.util.ByteString
import gg.duel.pinger.data.ParsedPongs._

import scala.annotation.tailrec

/** 01/02/14 */
object PongParser {

  def threadLocal[T](f: => T): ThreadLocal[T] =  ThreadLocal.withInitial[T](new Supplier[T] {
    override def get(): T = f
  })

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

  object UChar {
    def apply(byte: Byte) = byte.toChar & 0xFF
    def apply(int: Int): Int = int & 0xFF
  }

  object ##:: {
    def unapply(from: ByteString): Option[(Byte, ByteString)] =
      if ( from.isEmpty ) None
      else Some(from.head,from.tail)
  }

  class CubeReader(val data: ByteString) {
    val count = data.length
    var pos = 0
    def nextString(): String = {
      val stringBuilder = new java.lang.StringBuilder()
      var res = nextInt()
      while ( res != 0 && res != Int.MinValue ) {
        stringBuilder.append(CubeString.charMapping(UChar(res.toChar)))
        res = nextInt()
      }
      stringBuilder.toString
    }
    def nextIp(): String = {
      GetIp.get(data, pos).map(_._1).getOrElse("")
    }
    def nextInt(): Int = {
      if ( count <= pos ) Int.MinValue
      else {
        val first = data(pos)
        if (first == -128 && count >= pos + 3) {
          val a = UChar(data(pos + 1))
          val b = UChar(data(pos + 2))
          val r = a | (b << 8)
          pos = pos + 3
          r
        } else if (first == -127 && count >= pos + 5) {
          val m = UChar(data(pos + 1))
          val n = UChar(data(pos + 2))
          val o = UChar(data(pos + 3))
          val p = UChar(data(pos + 4))
          val r = ((m | (n << 8)) | o << 16) | (p << 24)
          pos = pos + 5
          r
        } else {
          pos = pos + 1
          first.toInt
        }
      }
    }
  }

  object GetInt {

    def fastbs(data: ByteString, pos: Int): Option[(Int, Int)] = {
      val count = data.length
      if ( count <= pos ) None
      else {
        val first = data(pos)
        if (first == -128 && count >= pos + 3) {
          val a = UChar(data(pos + 1))
          val b = UChar(data(pos + 2))
          val r = a | (b << 8)
          Some(r,3)
        } else if (first == -127 && count >= pos + 5) {
          val m = UChar(data(pos + 1))
          val n = UChar(data(pos + 2))
          val o = UChar(data(pos + 3))
          val p = UChar(data(pos + 4))
          val r = ((m | (n << 8)) | o << 16) | (p << 24)
          Some(r, 5)
        } else Some(first.toInt, 1)
      }
    }
    def fastbs2(data: ByteString, pos: Int, v: Array[Boolean], g: Array[Int]): Unit = {
      val count = data.length
      if ( count <= pos ) v(0) = false
      else {
        val first = data(pos)
        if (first == -128 && count >= pos + 3) {
          val a = UChar(data(pos + 1))
          val b = UChar(data(pos + 2))
          val r = a | (b << 8)
          v(0) = true
          g(0) = r
          g(1) = 3
        } else if (first == -127 && count >= pos + 5) {
          val m = UChar(data(pos + 1))
          val n = UChar(data(pos + 2))
          val o = UChar(data(pos + 3))
          val p = UChar(data(pos + 4))
          val r = ((m | (n << 8)) | o << 16) | (p << 24)
          v(0) = true
          g(0) = r
          g(1) = 5
        } else {
          v(0) = true
          g(0) = first.toInt
          g(1) = 1
        }
      }
    }

    @tailrec
    def getn(arr: Array[Int], data: ByteString, read_position: Int, total_required: Int, write_position: Int): Int = {
      if ( total_required == 0 ) read_position
      else fastbs(data, read_position) match {
        case Some((got_value, moved_position)) =>
          arr(write_position) = got_value
          getn(arr, data, read_position + moved_position, total_required - 1, write_position + 1)
        case None =>
          read_position
      }
    }

    def unapply(bytes: ByteString): Option[(Int, ByteString)] = {
      val v = Array(false)
      val g = Array(0, 0)
      fastbs2(bytes, 0, v, g)
      if ( v(0) ) Some((g(0), bytes.drop(g(1))))
      else None
    }
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

    val intToInt = Array[Int](
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
    )

    private val itsSize = intToInt.length

    def mapping(from: Int): Int = if ( from < itsSize ) intToInt(from) else from

    def charMapping(from: Int): Char = mapping(from).toChar

  }

  object GetString {

    @tailrec
    private def build(b: Array[Boolean], i: Array[Int], stringBuilder: java.lang.StringBuilder, byteString: ByteString, position: Int): Int = {
      GetInt.fastbs2(byteString, position, b, i)
      if (b(0)) {
        val result = i(0)
        val consumed = i(1)
        if (result == 0) consumed + position
        else {
          stringBuilder.append(CubeString.charMapping(UChar(result)))
          build(b, i, stringBuilder, byteString, position + consumed)
        }
      } else position
    }

    def ucharsSuperFast(byteString: ByteString, position: Int = 0): (String, Int) = {
      val stringBuilder = new java.lang.StringBuilder()
      val len = build(Array.fill(1)(false), Array.fill(2)(0), stringBuilder, byteString, position)
      stringBuilder.toString -> len
    }

    def unapply(bytes: ByteString): Option[(String, ByteString)] = bytes match {
      case ByteString.empty => None
      case something =>
        val (str, pos) = ucharsSuperFast(bytes)
        Some(str -> bytes.drop(pos))
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
        Option(PlayerCns(version, ids))
      case _ => None
    }
  }

  object GetIp {
    def get(from: ByteString, offset: Int): Option[(String, Int)] = {
      if ( from.length >= offset + 3 )
        Option(s"${UChar(from(offset))}.${UChar(from(offset + 1))}.${UChar(from(offset+2))}.x", offset + 3)
      else None
    }
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

    def unapply(list: ByteString): Option[PlayerExtInfo] = {
      val cr = new CubeReader(list)
      var isOk = cr.nextInt() == 0 && cr.nextInt() == 1 && cr.nextInt() == -1 && cr.nextInt() == ack
      val version = cr.nextInt()
      isOk = isOk && cr.nextInt() == 0 && cr.nextInt() == -11
      val cn = cr.nextInt()
      val ping = cr.nextInt()
      val name = cr.nextString()
      val team = cr.nextString()
      if ( !isOk ) None else Some(PlayerExtInfo(
        version = version, cn = cn, ping = ping, name = name, team = team,
        frags = cr.nextInt(), deaths = {cr.nextInt(); cr.nextInt()},
        teamkills = cr.nextInt(), accuracy = cr.nextInt(), health = cr.nextInt(), armour =  cr.nextInt(),
        gun = cr.nextInt(), privilege =  cr.nextInt(), state = cr.nextInt(), ip = cr.nextIp()
      ))
    }
    def unapply2(list: ByteString): Option[PlayerExtInfo] = list match {
      case 0 >>: 1 >>: -1 >>: `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>##:: team >>##:: frags >>: flags >>: deaths >>:
        teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
        >>: ip >~: _ =>
        val vinfo = PlayerExtInfo(version, cn, ping, name, team,
          frags, deaths, teamkills, accuracy, health, armour, gun, privilege, state, ip)
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
        Option(TeamScores(version, gamemode, remain, ascores))
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
        Option((TeamScore(name, score, baseMap = true, baseScores), leftOvers))
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
