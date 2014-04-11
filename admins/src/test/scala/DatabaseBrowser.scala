import akka.util.{ByteString, ByteStringBuilder}
import java.io.File
import java.nio.{ByteOrder, ByteBuffer}
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.{DB, Options}
import us.woop.pinger.client.Extractor
import us.woop.pinger.client.persistence.Format.{Server, DecodeServerDataKey, ServerIndexIndex, ServerIndexKey}
import us.woop.pinger.client.SauerbratenFormat.GetTeamScores
import us.woop.pinger.SauerbratenServerData.Conversions.{ConvertedTeamScore, ConvertedServerInfoReply}
import us.woop.pinger.SauerbratenServerData.PlayerExtInfo


object DatabaseBrowser extends App {

  val path = "/home/william/Projects/14/ladder.sauer/indexed-data/11-07-copy"
  val dbFile = new File(path)
  val options = new Options()
  val db = factory.open(dbFile, options)
  val it = db.iterator()
  it.seekToFirst()

  import collection.JavaConverters._

//  val yes = it.asScala.map{_.getKey}.collect {
//    case DecodeServerDataKey(server) => server
//    case ServerIndexIndex(idx) => idx
//    case ServerIndexKey(sserverIndex) => sserverIndex
//    case other => throw new Exception(new String(other))
//  }
//  it.seek(ServerIndexKey().toBytes)
//  it.next()
//
//  val serverIndices = it.asScala.map{buf => buf.getKey -> buf.getValue}.collect{
//    case (ServerIndexKey(server), firstIndex @ DecodeServerDataKey(ha)) =>
//      Option(server -> firstIndex)
//    case other => None
//  }.takeWhile{_.isDefined}.flatMap{_.toList}.toList
//  val decodeWoot = Extractor.extract.lift andThen {_.toSeq} andThen {_.flatten}
//  val serverDatums = for {
//    (server, index) <- serverIndices
//  } yield {
//    it.seek(index)
//    val superator = it.asScala.map{buf => buf.getKey -> buf.getValue}.collect {
//      case (DecodeServerDataKey(ha), rest) =>
//        Option(ha -> rest)
//      case _ => None
//    }.takeWhile{_.isDefined}.flatMap{_.toList}.flatMap{case(k, v) => decodeWoot(ByteString(v)).map{x => k -> x}}.collect {
//      case (k, p: PlayerExtInfo) => k -> p
//      case (k, p: ConvertedServerInfoReply) => k -> p
//    }.toList
//    server -> superator
//  }

//  println(serverDatums.take(5).map{case (a, b) => a -> b.toList.mkString("\n")}.mkString("\n---\n"))

  it.seekToFirst()

//  val yese = it.asScala.map{buf => buf.getKey -> buf.getValue}.collect {
//    case (DecodeServerDataKey(ha), stuff) => ha -> stuff
//    case (ServerIndexKey(ha), DecodeServerDataKey(lol)) => ha -> lol
//    case (ServerIndexIndex(_), _) => "Master"
//  } mkString "\n"

  def buf(data: java.util.Map.Entry[Array[Byte], Array[Byte]]): (Array[Byte], Array[Byte]) =
    data.getKey -> data.getValue

  val yese = it.asScala.map{buf}.collect {
    case (DecodeServerDataKey(ha), bytes) => ha -> ByteString(bytes)
  }.collect {
    case (ha, GetTeamScores(scores)) if scores.scores.nonEmpty => ha -> scores
  }
  it.seekToFirst()

//  println(it.asScala.length)

  println(yese mkString "\n")

//  println(serverIndices)
//  it.seek(Server("81.169.137.114",20000).toBytes)
//  val serverIndex = it.next().getValue
//  it.seek(serverIndex)
//  val DecodeServerDataKey(y) = it.next().getKey
//  println(y)
//  val DecodeServerDataKey(z) = it.next().getKey
//  println(z)
//  val DecodeServerDataKey(q) = it.next().getKey
//  println(q)
//  println(yes.toList.mkString("\n"))

  db.close()
}

