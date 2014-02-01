
import akka.actor._
import akka.event.LoggingReceive
import akka.io
import akka.io.Udp
import akka.util.ByteString
import java.io.{InputStreamReader, BufferedReader, DataOutputStream}
import java.net.{InetSocketAddress, Socket}
import java.security.MessageDigest
import scala.tools.cmd.Parser.ParseException
import scala.util.control.NonFatal
import scala.util.{Random, Try}

/** 31/01/14 */
object Parpa extends App {

  object PingerActor {

    case class InvalidMessage(from: InetSocketAddress, bytes: List[Byte])
    case class Ping(host: InetSocketAddress)
    case object Ready
    case class InvalidMessageFormatException(response: List[Int]) extends Exception(s"An incompatible message received: $response")

    object AcceptableBytes {
      def unapply(bytes: ByteString): Option[Stream[Byte]] = {
        bytes.toStream match {
          case GetInt(_, GetInt(_, GetInt(_, rest))) => Option(rest)
          case _ => None
        }
      }
    }
  }


  class PingerActor(listener: ActorRef) extends Actor with ActorLogging {

    import PingerActor._

    val myAddress = new InetSocketAddress("0.0.0.0", 0)

    import context.system
    io.IO(Udp) ! Udp.Bind(self, myAddress)

    def receive = {
      case Udp.Bound(boundTo) =>
        log.debug(s"Bound to $boundTo")
        val socket = sender
        listener ! Ready
        context.become(ready(socket))
    }

    // TODO add some salting/hashing to prevent UDP source spoofing.
    // We can shove in the hash of the IP with a salt that changes frequently.
    // Enough to make any noobs unable to keep up. Like every five minutes.
    // We then keep track of the appropriate salts.
    // Very important - don't want cunts fucking things up epicly.

    val random = new Random
    var hashes = scala.collection.mutable.HashMap[InetSocketAddress, List[Byte]]()
    val hasher = MessageDigest.getInstance("SHA")
    def makeHash(address: InetSocketAddress): List[Byte] = {
      val inputBytes = s"${random.nextString(6)}$address".toCharArray.map(_.toByte)
      val hashedBytes = hasher.digest(inputBytes)
      val postfix = hashedBytes.toList.take(10)
      hashes += address -> postfix
      postfix
    }
    val askForServerInfo = List(1, 1, 1).map(_.toByte)
    val askForPlayerStats1 = List(0, 0, -1).map(_.toByte)
    val askForPlayerStats2 = List(0, 1, -1).map(_.toByte)
    val askForPlayerStats3 = List(0, 2, -1).map(_.toByte)

    val outboundMessages = List(askForServerInfo, askForPlayerStats1, askForPlayerStats2, askForPlayerStats3)

    val targets = scala.collection.mutable.HashSet[InetSocketAddress]()

    def ready(send: ActorRef): Receive = {
      case Ping(who) =>
        println("Sending ping!")
        val hash = makeHash(who)
        targets += who
        for {
          message <- outboundMessages
          hashedMessage = message ::: hash
          hashedArray = hashedMessage.toArray
          byteString = ByteString(hashedArray)
        } {
          println(s"Sending $byteString to $who")
          send ! Udp.Send(byteString, who)
        }

      case Udp.Received(receivedBytes, fromWho) => //if targets contains fromWho =>
        println(receivedBytes, fromWho)
        val ourHash = hashes(fromWho)
        val (head, rest) = receivedBytes.toList.splitAt(3)
        val (theirHash, message) = rest.toList.splitAt(10)
        theirHash match {
          case `ourHash` =>

            val fn: PartialFunction[Stream[Byte], Any] = matchers orElse {case x => InvalidMessage(fromWho, x.toList)}

            //bytes match {
            val result = fn(message.toStream)
            println(fromWho, result)

          case wrongHash =>
            log.warning(s"Wrong hash received from $fromWho. Expected hash: $ourHash, received hash: $wrongHash, head: $head, Message: $receivedBytes")
        }


      case Udp.Received(AcceptableBytes(bytesx), fromWho) if targets contains fromWho =>

      case Udp.Received(otherBytes, fromWho) => //if targets contains fromWho =>
        log.warning(s"Message from $fromWho does not match an acceptable format. $otherBytes")
      case Udp.Received(otherBytes, fromWho) =>
        log.warning(s"Message from an unexpected target: $fromWho - $otherBytes")
    }

    val matchers: PartialFunction[Stream[Byte], Any] = {
      case GetPlayerExtInfo(x) => x
      case GetServerInfoReply(x) => x
      case GetPlayerCns(x) => x
      case GetHopmodUptime(x) => x
      case GetTeamScores(x) => x
      case GetUptime(x) => x
    }
  }
  val as = ActorSystem("heloo")
  import akka.actor.ActorDSL._


  val receiver = actor(as)(new Act {
    import PingerActor._
    become {
    case Ready =>
      println("Ready!")

      import PingerActor._
      println("Ready once.")
      //val sservers = ("81.169.137.114", 20000) :: Nil
      val sservers = ("85.10.195.182",30000)::Nil
      for {
        (h, p) <- servers
        sock = new InetSocketAddress(h, p + 1)
      } sender ! Ping(sock)

    case x => println(x)
  } })


  //val f = ({ case GetServerInfoReply(r) => r }).li


  // List((109.73.51.58,10050), (109.73.51.58,28785), (109.73.51.58,10044), (134.0.24.218,28785), (108.174.48.250,80), (89.35.181.113,35000), (173.255.193.121,28785), (89.35.181.113,28785), (5.175.166.93,28785), (172.245.41.76,28785), (88.198.108.163,28785), (85.214.214.91,28785), (31.31.203.105,28785), (192.81.216.48,20000), (95.85.28.218,10000), (95.85.28.218,30000), (95.85.28.218,60000), (95.85.28.218,40000), (95.85.28.218,50000), (95.85.28.218,20000), (188.40.118.114,28785), (188.40.118.113,28785), (85.214.29.156,10000), (85.214.29.156,20000), (85.214.29.156,40000), (144.76.157.141,10000), (176.9.75.98,10070), (109.73.51.58,10010), (109.73.51.58,10030), (96.231.199.244,28785), (81.169.137.114,10000), (81.169.137.114,20000), (81.169.137.114,30000), (87.98.139.152,28785), (83.169.44.106,14000), (96.231.199.244,28787), (85.214.29.156,30000), (85.214.214.91,28900), (195.34.136.67,20000), (91.211.244.91,28785), (91.211.244.91,28795), (91.211.244.91,20000), (109.74.195.195,28899), (109.74.195.195,28785), (85.214.66.181,6666), (85.214.66.181,10000), (120.138.18.82,28785), (95.156.230.73,32370), (78.46.73.228,30000), (78.46.73.228,10000), (78.46.73.228,20000), (78.46.73.228,40000), (78.46.73.228,50000), (74.91.115.200,28785), (178.4.3.99,28785), (188.164.130.6,4999), (85.114.142.164,20000), (79.110.128.93,28785), (79.110.128.93,20000), (31.220.51.27,24987), (37.187.5.145,50000), (37.187.5.145,20000), (37.187.5.145,10000), (37.187.5.145,40000), (37.187.5.145,30000), (188.164.130.6,1111), (176.28.47.108,28785), (134.255.220.143,28785), (88.191.246.101,28785), (88.191.246.101,10000), (88.191.246.101,20010), (88.191.246.101,20020), (155.185.232.162,28785), (188.164.130.6,34234), (188.164.130.6,25010), (69.195.137.18,28789), (69.195.137.18,28785), (178.16.34.167,28785), (62.112.211.12,28785), (23.94.28.21,50010), (23.94.28.21,50030), (23.94.28.21,50000), (78.47.184.3,1337), (178.63.3.34,4999), (178.63.3.34,3999), (178.63.3.34,1999), (178.63.3.34,1111), (217.115.159.248,28785), (188.164.130.6,3335), (188.164.130.6,25000), (188.164.130.6,12345), (78.129.230.93,28785), (83.117.66.133,28785), (188.164.130.6,25040), (188.164.130.6,25030), (188.164.130.6,4446), (91.211.244.91,4060), (91.211.244.91,4070), (91.211.244.91,4020), (192.81.216.48,30000), (50.21.129.159,28785), (192.81.216.48,10000), (31.220.48.22,38785), (178.238.226.172,28785), (192.95.30.66,28785), (192.81.216.48,40000), (62.75.213.175,1234), (69.136.162.167,25000), (91.211.244.91,4010), (91.211.244.91,4050), (91.211.244.91,4040), (91.211.244.91,4030), (91.211.244.91,4000), (23.94.28.21,50020), (80.117.69.202,54345), (192.81.216.48,50000), (78.47.23.219,28785), (62.75.213.175,10000), (138.91.116.177,28785), (62.75.213.175,28785), (62.75.213.175,1337), (178.32.95.181,28785), (184.22.86.114,28785), (146.185.167.115,28785), (5.135.216.194,28785), (5.135.216.194,11100), (5.135.216.194,28796), (188.164.130.6,50000), (188.164.130.6,50010), (188.164.130.6,50020), (188.164.130.6,50030), (5.231.58.11,28785), (5.231.58.11,2000), (62.75.213.175,20000), (188.164.130.6,25020), (188.164.130.6,3999), (188.164.130.6,29785), (188.164.130.6,28785), (75.129.117.137,28785), (188.164.130.6,22224), (84.149.240.180,28785), (188.164.130.6,35234), (185.38.46.194,60010), (185.38.46.194,60000), (188.164.130.6,1999), (185.38.46.194,61234), (93.209.15.202,10020), (188.164.130.6,26030), (188.164.130.6,26010), (188.164.130.6,26060), (188.164.130.6,26040), (188.164.130.6,26020), (188.164.130.6,26000), (188.164.130.6,26050), (188.164.130.6,26070), (24.138.248.86,28785), (85.10.195.182,11011), (85.10.195.182,28785), (85.10.195.182,30000), (85.10.195.182,50000), (79.93.160.11,28785), (69.136.162.167,35000), (68.150.161.181,20000), (95.85.48.85,28785), (188.164.130.6,22222))

    val pa = as.actorOf(Props(classOf[PingerActor], receiver))

  def getServers(masterServer: (String, Int)): List[(String, Int)] = {
    val serverRegex = """^addserver (\d+\.\d+\.\d+\.\d+) (\d+)$""".r
    val socket = new Socket(masterServer._1, masterServer._2)
    try {
      val dataOutputStream = new DataOutputStream(socket.getOutputStream)
      try {
        val dataInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream))
        try {
          dataOutputStream.writeBytes("list\n")
          def getLine = Try(Option(dataInputStream.readLine)).toOption.flatten.filterNot{_ == '\0'.toString}
          val contents = Stream.continually(getLine).takeWhile(_ != None).flatten.toList
          try {
            contents.map{ case serverRegex(host, port) => (host, port.toInt)}
          } catch {
            case NonFatal(e) => throw new ParseException(s"Failed to parse contents due to '$e' - contents: '$contents'")
          }
        } finally {
          dataInputStream.close()
        }
      } finally {
        dataOutputStream.close()
      }
    } finally {
      socket.close()
    }
  }

  lazy val servers = getServers(sauerMasterserver)

  val sauerMasterserver = ("sauerbraten.org", 28787)
//  println(servers)

  object GetUChar {
    def unapply(byte: Byte): Option[Int] =
      Option(byte.toChar & 0xFF)
  }

  object GetInt {
    def unapply(bytes: Stream[Byte]): Option[(Int, Stream[Byte])] = bytes match {
      case -127 #:: GetUChar(m) #:: GetUChar(n) #:: GetUChar(o) #:: GetUChar(p) #:: rest=>
        Option(((m | (n<<8)) | o<<16)|(p<<24), rest)
      case -128 #:: GetUChar(m) #:: n #:: rest =>
        Option(m | (n << 8), rest)
      case n #:: rest =>
        Option((n, rest))
      case Stream.Empty =>
        None
    }
  }

  object GetInts {
    def ints(bytes: Stream[Byte]): Stream[(Int, Stream[Byte])] = {
      bytes match {
        case GetInt(value, rest) => (value, rest) #:: ints(rest)
        case Stream.Empty => Stream.Empty
      }
    }
    def unapply(bytes: Stream[Byte]): Option[Stream[Int]] =
      Option(ints(bytes).map(_._1))
  }
  object GetString {
    def unapply(bytes: Stream[Byte]): Option[(String, Stream[Byte])] = bytes match {
      case Stream.Empty =>
        None
      case something =>
        val (forStr, rest) = GetInts.ints(bytes).span(_._1 != 0)
        Option(forStr.map(_._1.toChar).mkString, rest.take(1).flatMap(_._2))
    }
  }

  ByteString(61,22,43,54,0).toStream match {
    case GetString(whoo, rest) => println(whoo, rest.toList)
    case x => println(x)
  }



  val >>:: = GetString


  val >>: = GetInt


  case class ServerInfoReply(clients: Int, protocol: Int, gamemode: Int, remain: Int, maxclients: Int,
  gamepaused: Option[Int], gamespeed: Option[Int], mapname: String, desc: String)

  object GetServerInfoReply {
    def unapply(stream: Stream[Byte]): Option[ServerInfoReply] = stream match {
      case clients >>: numattrs >>: protocol >>: gamemode >>:
        remain >>: maxclients >>: pass >>: gamepaused >>:
        gamespeed >>: mapname >>:: desc >>:: rest if numattrs == 7 =>
        Option(ServerInfoReply(clients, protocol, gamemode, remain, maxclients, Option(gamepaused), Option(gamespeed), mapname, desc))
      case clients >>: numattrs >>: protocol >>: gamemode >>:
        remain >>: maxclients >>: pass >>: mapname >>:: desc >>:: rest if numattrs == 5 =>
        Option(ServerInfoReply(clients, protocol, gamemode, remain, maxclients, None, None, mapname, desc))
      case _ => None
    }
  }

  val ack = -1

  case class Uptime(version: Int, totalsecs: Int)

  case class HopmodUptime(uptime: Uptime, hopmodVersion: Int, hopmodRevision: Int, buildTime: String)

  object GetHopmodUptime {
    def unapply(stream: Stream[Byte]): Option[HopmodUptime] = stream match {
      case `ack` >>: version >>: totalsecs >>: isHopmod >>: hopmodVersion >>: hopmodRevision >>: buildTime >>:: Stream.Empty =>
        //Option(Uptime(version, totalsecs, Option(isHopmod), Option(hopmodVersion), Option(hopmodRevision), Option(buildTime)))
        Option(HopmodUptime(Uptime(version, totalsecs), hopmodVersion, hopmodRevision, buildTime))
      case _ => None
    }
  }
  object GetUptime {
    def unapply(stream: Stream[Byte]): Option[Uptime] = stream match {
      case `ack` >>: version >>: totalsecs >>: Stream.Empty =>
        Option(Uptime(version, totalsecs))
      case _ => None
    }
  }


  case class PlayerCns(version: Int, cns: List[Int])
  object GetPlayerCns {
    def unapply(stream: Stream[Byte]): Option[PlayerCns] = stream match {
      case `ack` >>: version >>: 0 >>: -10 >>: GetInts(ids) =>
        Option(PlayerCns(version, ids.toList))
      case _ => None
    }
  }
  case class PlayerExtInfo(version: Int, cn: Int, ping: Int, name: String, team: String, frags: Int,
  deaths: Int, teamkills: Int, accuracy: Int, health: Int, armour: Int, gun: Int, privilege: Int, state: Int, ip: String)

  object GetIp {
    def unapply(stream: Stream[Byte]): Option[(String, Stream[Byte])] = stream match {
      case GetUChar(a) #:: GetUChar(b) #:: GetUChar(c) #:: rest =>
        Option(s"$a.$b.$c.x", rest)
      case _ =>
        None
    }
  }
  val >~: = GetIp
  object GetPlayerExtInfo {
    def unapply(stream: Stream[Byte]): Option[PlayerExtInfo] = stream match {
      case `ack` >>: version >>: 0 >>: -11 >>:
        cn >>: ping >>: name >>:: team >>:: frags >>: flags >>: deaths >>:
        teamkills >>: accuracy >>: health >>: armour >>: gun >>: privilege >>: state
        >>: ip >~: Stream.Empty =>
        Option(PlayerExtInfo(version, cn, ping, name, team, frags, deaths, teamkills, accuracy, health, armour,
        gun, privilege, state, ip))
      case _ => None
    }
  }
  case class TeamScores(version: Int, gamemode: Int, remain: Int, scores: List[TeamScore])
  object GetTeamScores {
    def unapply(stream: Stream[Byte]): Option[Any] = stream match {
      case `ack` >>: version >>: 1 >>: gamemode >>: remain >>: Stream.Empty =>
        Option(TeamScores(version, gamemode, remain, Nil))
      case `ack` >>: version >>: 0 >>: gamemode >>: remain >>: scores =>
        val ret = GetTeamScore.many(scores)
        val ascores = ret.map(_._1)
        Option(TeamScores(version, gamemode, remain, ascores.toList))
      case _ =>
        None
    }
  }
  case class TeamScore(name: String, score: Int, baseMap: Boolean, baseScores: List[Int])
  object GetTeamScore {
    def ints(bytes: Stream[Byte]): Stream[(Int, Stream[Byte])] = {
      bytes match {
        case GetInt(value, rest) => (value, rest) #:: ints(rest)
        case Stream.Empty => Stream.Empty
      }
    }
    def unapply(stream: Stream[Byte]): Option[(TeamScore, Stream[Byte])] = stream match {
      case name >>:: score >>: -1 >>: rest =>
        Option(TeamScore(name, score, baseMap = false, Nil), rest)
      case name >>:: score >>: numBases >>: rest =>
        val collected = GetInts.ints(rest).take(numBases)
        val baseScores = collected.map(_._1)
        val leftOvers = collected.lastOption.map(_._2).getOrElse(Stream.Empty)
        Option(TeamScore(name, score, baseMap = true, baseScores.toList), leftOvers)
      case _ =>
        None
    }
    def many(bytes: Stream[Byte]): Stream[(TeamScore, Stream[Byte])] = {
      bytes match {
        case GetTeamScore(teamScore, rest) => (teamScore, rest) #:: many(rest)
        case Stream.Empty => Stream.Empty
        case other =>
          println(s"[WARNING] Unknown teamScore data: $other. Ignoring it.")
          Stream.Empty
      }
    }
  }



  val bytes = List(1, 5, -128, 3, 1, 4, -128, 85, 2, -128, -128, 0, -1, 118, 101, 110, 105, 99, 101, 0, 86, 101, 110, 105, 99, 101, 32, 78, 111, 49, 0)

  val pf: PartialFunction[Stream[Byte], Any] = { case GetServerInfoReply(r) => r }
  val paf: PartialFunction[Stream[Byte], Any] = {
    case clients >>: numattrs >>: protocol >>: gamemode >>:
      remain >>: maxclients >>: pass >>: mapname >>:: desc >>:: rest =>
      (clients, numattrs, protocol, gamemode, remain, maxclients, pass, mapname, desc, rest.toList)
  }
  val lpf = paf.lift
  val bs = bytes.toStream.map(_.toByte)
//  println(bs)
  println(lpf(bs))
  println(GetInt.unapply(bs.drop(1)))






//  val teamInfos = List(-1, 105, 0, 10, 20, 101, 118, 105, 108, 0, -128, -105, 0, 4, 0, 1, 3, 5, 103, 111, 111, 100, 0, 89, 2, 2, 4)
//
//  val r = GetTeamScores.unapply(teamInfos.toStream.map(_.toByte))
//  println(r)
}

