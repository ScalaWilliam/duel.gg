import akka.util.ByteStringBuilder
import java.io.File
import java.nio.{ByteOrder, ByteBuffer}
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.{DB, Options}
import us.woop.pinger.client.persistence.Format.{Server, DecodeServerDataKey, ServerIndexIndex, ServerIndexKey}


object DatabaseBrowser extends App {


  val path = "/home/william/Projects/14/ladder.sauer/indexed-data"
  val dbFile = new File(path)
  val options = new Options()
  val db = factory.open(dbFile, options)
  val it = db.iterator()
  it.seekToFirst()

  import collection.JavaConverters._

  val yes = it.asScala.map{_.getKey}.collect {
    case DecodeServerDataKey(server) => server
    case ServerIndexIndex(idx) => idx
    case ServerIndexKey(sserverIndex) => sserverIndex
    case other => throw new Exception(new String(other))
  }
  it.seek(ServerIndexKey().toBytes)
  it.next()

  val serverIndices = it.asScala.map{buf => buf.getKey -> buf.getValue}.collect{
    case (ServerIndexKey(server), firstIndex @ DecodeServerDataKey(ha)) =>
      Option(server -> firstIndex)
    case other => None
  }.takeWhile{_.isDefined}.flatMap{_.toList}.toList

  val serverDatums = for {
    (server, index) <- serverIndices
  } yield {
    it.seek(index)
    val superator = it.asScala.map{buf => buf.getKey -> buf.getValue}.collect {
      case (DecodeServerDataKey(ha), rest) =>
        Option(ha -> rest)
      case _ => None
    }.takeWhile{_.isDefined}.flatMap{_.toList}.toList
    server -> superator
  }

  println(serverDatums.take(5).map{case (a, b) => a -> b.toList}.mkString("\n"))

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
