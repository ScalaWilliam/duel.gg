import akka.util.ByteString
import java.nio.{ByteOrder, ByteBuffer}
import scala.annotation.tailrec
import us.woop.pinger.data.ParsedPongs
import ParsedPongs.Conversions.ConvertServerInfoReply
import ParsedPongs.PlayerExtInfo
import us.woop.pinger.Extractor

object Aggregator {

  def process(seq: InputSequence) = {
    val playerToInfos = seq.playerInfos.filterNot{pi => Privilege(pi.privilege).isSpectating}.groupBy{playerInfo => (playerInfo.name, playerInfo.ip)}
    val aggregatedInfos = playerToInfos.map{
      case ((name, ip), infos) =>
        val accuracy = infos.map{_.accuracy}.sum / infos.size
        val favouriteWeapon = infos.map{i=>Weapon(i.gun)}.groupBy{identity}.mapValues(_.size).toList.sortBy{_._2}.map{_._1}.last
        val deaths = infos.map{_.deaths}.sum
        val frags = infos.map{_.frags}.sum
        val teamkills = infos.map{_.teamkills}.sum
        PlayerScore(name = name, ip = ip, accuracy = accuracy, favouriteWeapon = favouriteWeapon, deaths = deaths, frags = frags, teamkills = teamkills)
    }
    OutputSequence(server = seq.server, mode = MapMode(GameMap(seq.map), GameMode(seq.mode)), start = seq.start, duration = seq.duration, playerInfos = aggregatedInfos.toList)
  }

  def isSwitch(from: ConvertedServerInfoReply, to: ConvertedServerInfoReply) =
    from.remain < to.remain || from.mapname != to.mapname || from.gamemode != to.gamemode

  case class InputSequence(server: Server, mode: String, map: String, start: Long, duration: Option[Int], playerInfos: List[PlayerExtInfo])
  case class Server(address: String)
  case class Weapon(id: Int)
  case class Privilege(id: Int) {
    def isSpectating = false
  }
  case class PlayerScore(name: String, ip: String, accuracy: Int, favouriteWeapon: Weapon, deaths: Int, frags: Int, teamkills: Int)
  case class GameMap(map: String)
  case class GameMode(mode: String)
  case class MapMode(map: GameMap, mode: GameMode)
  case class OutputSequence(server: Server, mode: MapMode, start: Long, duration: Option[Int], playerInfos: List[PlayerScore])
}
object AggregatorRun extends App {
  import Aggregator._
  case class Key(index: Long, seq: Int, ip: String, port: Int)
  def decodeKey(key: Array[Byte])  = {
    val bb = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN)
    val idx = bb.getLong
    val seq = bb.getInt
    val ip = ByteBuffer.allocate(4)
    bb.get(ip.array(), 0, 4)
    val port = bb.getInt
    Key(idx, seq, ip.array().map{_.toInt & 0xFF}.mkString("."), port)
  }
  object DecodedKey {
    def unapply(key: Array[Byte]):Option[Key] = Option(decodeKey(key))
  }
  object GetByteString {
    def unapply(data: Array[Byte]): Option[ByteString] = Option(ByteString(data))
  }
  @tailrec
  final def groupByGame(currentItem: Option[ConvertedServerInfoReply],
                  currentAggregate: Stream[PlayerExtInfo],
                  aggregated: Stream[(ConvertedServerInfoReply, Stream[PlayerExtInfo])],
                  left: Stream[Either[PlayerExtInfo, ConvertedServerInfoReply]]): Stream[(ConvertedServerInfoReply, Stream[PlayerExtInfo])] = {
    (currentItem, left) match {
      case (None, Right(infoReply) #:: rest) =>
        groupByGame(Option(infoReply), currentAggregate, aggregated, rest)
      case (None, Left(_) #:: rest) =>
        groupByGame(None, currentAggregate, aggregated, rest)
      case (None, Stream.Empty) =>
        aggregated
      case (Some(ci), Right(infoReply) #:: rest) if isSwitch(ci, infoReply) && currentAggregate.nonEmpty =>
        groupByGame(Option(infoReply), Stream.Empty, aggregated :+ ci -> currentAggregate, rest)
      case (Some(ci), Right(infoReply) #:: rest) if isSwitch(ci, infoReply) =>
        groupByGame(Option(infoReply), Stream.Empty, aggregated, rest)
      case (Some(ci), Right(_) #:: rest) =>
        groupByGame(Option(ci), currentAggregate, aggregated, rest)
      case (Some(ci), Left(item) #:: rest) =>
        groupByGame(Option(ci), currentAggregate :+ item, aggregated, rest)
      case (Some(ci), Stream.Empty) =>
        groupByGame(None, Stream.Empty, aggregated :+ ci -> currentAggregate, Stream.Empty)
    }
  }

  DbInstance.withDb() {
    db =>
    val it = db.iterator()
    it.seekToFirst()
    val decodeWoot = Extractor.extract.lift
    import collection.JavaConverters._
    val uniqueKeys = it.asScala.map {
      i =>
        val dbKey = i.getKey
        val parsedKey = decodeKey(dbKey)
        Server(s"${parsedKey.ip}:${parsedKey.port}")
    }.take(5000).toList.groupBy{identity}.mapValues{_.size}.toList.sortBy{_._2}.reverse.map{_._1}


    println(uniqueKeys)
    it.seekToFirst()

    it.asScala.map{ i=>
      val dbKey = i.getKey
      val parsedKey = decodeKey(dbKey)
      Server(s"${parsedKey.ip}:${parsedKey.port}")
    }.take(5000).foreach{println}

    it.close()
//    val serverData =
//    val herpen = uniqueKeys.mapValues {
//      items =>
//            val flatter =
//        items.map {
//          _.getValue
//        }.map {
//          ByteString(_)
//        }.flatMap {
//          x => decodeWoot(x)
//        }.flatten.collect {
//          case item: PlayerExtInfo => Left(item)
//          case item: ConvertedServerInfoReply => Right(item)
//        }
//        groupByGame(None, Stream.Empty, Stream.Empty, flatter)
//    }
//    val fifth = Server("81.169.137.114:20000")
//    val interest = herpen(fifth)
//
//    val derpen = for {
//      (server, coverMe) <- List(fifth -> interest) //herpen.take(4) //List(fifth -> interest)
//      (startOfGame, gameStream) <- coverMe
//    } yield {
//      val inputSeq = InputSequence(server = server, mode = s"${startOfGame.gamemode}",
//      map = startOfGame.mapname, start = 123L, duration = None, playerInfos = gameStream.toList)
//      process(inputSeq)
//    }
//    val oz = process(InputSequence(server=Server("lol"), mode = "ho", map = "ho", start = 0L, duration = None, playerInfos = herpen.head._2.flatMap{_._2}))
//      println(oz)
//
//    println(derpen)
//    println(uniqueKeys)
//    println(herpen)
//    val perServer = reduced.toSeq.groupBy{i => Server(i._1.ip+":"+i._1.port)}.mapValues(_.sortBy{_._1.index}.collect{
//      case item: PlayerExtInfo => Left(item)
//      case item: ConvertedServerInfoReply => Right(item)
//    })
//
//
//    println(perServer)
//
//    val woot = perServer.mapValues{d => d.foldLeft((Option.empty[ConvertedServerInfoReply], List.empty[PlayerExtInfo], List.empty[(ConvertedServerInfoReply, List[PlayerExtInfo])])){
//      case ((Some(currentGame), collected, olderGames), Right(serverInfoReply)) if isSwitch(currentGame, serverInfoReply) =>
//        (Option(serverInfoReply), Nil, olderGames :+ currentGame -> collected)
//      case ((Some(currentGame), collected, olderGames), Left(playerInfo)) =>
//        (Option(currentGame), collected :+ playerInfo, olderGames)
//      case ((None, _, _), Right(serverInfo)) =>
//        (Option(serverInfo), Nil, Nil)
//      case _ =>
//        (None, Nil, Nil)
//    }}
//
//    println(woot)

      it.close()
  }
}