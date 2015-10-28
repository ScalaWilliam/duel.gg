package gg.duel.pinger.data.journal
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}
import gg.duel.pinger.data.Server
import scala.collection.mutable.ArrayBuffer

class SauerBytesWriterTest extends WordSpec with Matchers {
  "Sauer bytes writer" must {
    "Convert forth and back correctly" in {
      val writer = new SauerBytesBufferWriter()

      val byteString = ByteString(1, 1, 1, 5, 5, -128, 3, 1, 3, -128, -61, 1, 17,
        1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0
      )
      val receivedBytes = SauerBytes(Server("127.1.1.1", 1234), 12314, byteString.toVector)
      val receivedBytes2 = SauerBytes(Server("127.1.2.1", 1235), 12311, byteString.toVector)

      writer.writeSauerBytes(receivedBytes)
      writer.writeSauerBytes(receivedBytes2)
      writer.writeSauerBytes(receivedBytes)

      val reader = new SauerByteArrayReader(writer.buffer.toArray)
      val list = reader.toIterator.toList

      // just checking - this is quite superfluous though..
      list shouldBe Vector(receivedBytes, receivedBytes2, receivedBytes)
    }
  }
}