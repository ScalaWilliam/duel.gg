package gg.duel.pinger.data.journal
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}
import gg.duel.pinger.data.Server
import scala.collection.mutable.ArrayBuffer

class SauerBytesWriterTest extends WordSpec with Matchers {
  "Sauer bytes writer" must {
    "Convert forth and back correctly" in {
      val buffer = ArrayBuffer.empty[Byte]
      val writer = SauerBytesWriter.createInjectedWriter(b => buffer.append(b:_*))

      val byteString = ByteString(1, 1, 1, 5, 5, -128, 3, 1, 3, -128, -61, 1, 17,
        1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0
      )
      val receivedBytes = SauerBytes(Server("127.1.1.1", 1234), 12314, byteString.toVector)
      val receivedBytes2 = SauerBytes(Server("127.1.2.1", 1235), 12311, byteString.toVector)

      writer(receivedBytes)
      writer(receivedBytes2)
      writer(receivedBytes)

      val getBytesFromArrayBuffer = SauerBytesWriter.arrayNumBytes(buffer.toArray)
      def reader = SauerBytesWriter.readSauerBytes(getBytesFromArrayBuffer)

      val list = Iterator.continually(reader).takeWhile(_.isDefined).map(_.get).toVector

      // just checking - this is quite superfluous though..
      list shouldBe Vector(receivedBytes, receivedBytes2, receivedBytes)
    }
  }
}