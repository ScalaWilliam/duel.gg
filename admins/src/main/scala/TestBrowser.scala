import akka.util.ByteString
import java.io.File
import java.nio.{ByteOrder, Bits, ByteBuffer}
import java.util.Comparator
import org.iq80.leveldb.{DBComparator, Options}
import us.woop.pinger.client.Extractor
import us.woop.pinger.client.SauerbratenFormat.GetServerInfoReply
import us.woop.pinger.SauerbratenServerData.Conversions.ConvertedServerInfoReply
import us.woop.pinger.SauerbratenServerData.ServerInfoReply
import vaadin.scala.UI
import vaadin.scala._
import vaadin.scala.server.ScaladinRequest
import vaadin.scala.internal.UriFragmentChangedListener
import vaadin.scala.server.Page

class TestBrowser extends UI {
  content = Button("Click me!", Notification.show("Hello, Scaladin!"))
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
//
//  def ddata = {
//    DbInstance.withDb(new Options {
//      comparator(new DBComparator {
//
//        def name = "Grandez"
//
//        def findShortSuccessor(key: Array[Byte]): Array[Byte] = key
//
//        def findShortestSeparator(start: Array[Byte], limit: Array[Byte]): Array[Byte] = start
//
//        def compare(x: Array[Byte], y: Array[Byte]): Int = {
//          val a = ByteBuffer.wrap(x).order(ByteOrder.LITTLE_ENDIAN)
//          val b = ByteBuffer.wrap(x).order(ByteOrder.LITTLE_ENDIAN)
//          val av = a.getLong
//          val bv = b.getLong
//          implicitly[Ordering[Long]].compare(av, bv)
//        }
//
//      })
//    }){ db =>
//
//      import collection.JavaConverters._
//      val it = db.iterator()
//      it.seekToFirst()
//      val decodeWoot = Extractor.extract.lift
//      it.asScala.flatMap {
//        i =>
//          val key = decodeKey(i.getKey)
//          decodeWoot(ByteString(i.getValue)).toSeq.flatten.map {
//            j => key -> j
//          }
//      }.map {
//        case (key, value) =>
//          s"$key" -> Map(
//            "key" -> s"$key",
//            "val" -> s"$value").toSeq
//      }.take(500000).toList
//
//    }

//  val tab = new Table()
//  tab.container= Container(ddaata: _*)
//  content = tab

  def ddaata = DbInstance.withDb(new Options {
    comparator(new DBComparator {

      def name = "Grandez"

      def findShortSuccessor(key: Array[Byte]): Array[Byte] = key

      def findShortestSeparator(start: Array[Byte], limit: Array[Byte]): Array[Byte] = start

      def compare(x: Array[Byte], y: Array[Byte]): Int = {
        val a = ByteBuffer.wrap(x).order(ByteOrder.LITTLE_ENDIAN)
        val b = ByteBuffer.wrap(x).order(ByteOrder.LITTLE_ENDIAN)
        val av = a.getLong
        val bv = b.getLong
        implicitly[Ordering[Long]].compare(av, bv)
      }

    })
  }) { db =>
    val iterator = db.iterator()
    iterator.seekToFirst()
    import collection.JavaConverters._

    object DecodedKey {
      def unapply(key: Array[Byte]):Option[Key] = Option(decodeKey(key))
    }
    object GetByteString {
      def unapply(data: Array[Byte]): Option[ByteString] = Option(ByteString(data))
    }
    val codedIterator = iterator.asScala map { i => i.getKey -> i.getValue } collect { case (DecodedKey(key), data) => key -> data }
    type TTT = (Key, Array[Byte])
    val goog = collection.mutable.TreeSet.empty[TTT](new Ordering[TTT]{
      def compare(x: TTT, y: TTT): Int =
        implicitly[Ordering[Long]].compare(x._1.index, y._1.index)
    })
    codedIterator take 500 foreach { goog += _ }
      val red = goog.map{
        case (a, b) =>
          a.toString -> List("a"->a.toString, "b"->b.toString)
      }
    red
//    val serverReplies = iterator.asScala.map{ i => i.getKey -> i.getValue }.collect { case (DecodedKey(key), GetByteString(GetServerInfoReply(reply))) => key -> ConvertedServerInfoReply.convert(reply) }
//
//    serverReplies.take(50).toSeq.map{case (a, b) => s"$a" -> Seq("ok"->s"$a", "o"->s"$b")}

  }
}