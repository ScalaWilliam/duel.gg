package us.woop.pinger.data.persistence

import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}
import us.woop.pinger.data.Server
import us.woop.pinger.data.log.SauerBytes

import scala.collection.mutable.ArrayBuffer

class SauerReaderWriterTest extends WordSpec with Matchers {
  "SauerWriter" must {
    "Write to array buffer and back" in {

      val byteString = ByteString(1, 1, 1, 5, 5, -128, 3, 1, 3, -128, -61, 1, 17,
        1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0
      )
      val receivedBytes = SauerBytes(Server("127.1.1.1", 1234), 12314, byteString.toVector)
      val arrayBuffer = ArrayBuffer.empty[Byte]
      val sauerWriter = SauerReaderWriter.writeToArrayBuffer(arrayBuffer)

      val receivedBytes2 = receivedBytes.copy(
        server = receivedBytes.server.copy(port = 991),
        message = receivedBytes.message :+ 123.toByte
      )

      sauerWriter.write(receivedBytes)
      sauerWriter.write(receivedBytes2)
      sauerWriter.flush()
      sauerWriter.close()

      arrayBuffer should not be empty
      info(s"Array buffer: $arrayBuffer")
      val receivedBytesIterator = SauerReaderWriter.readFromByteIterator(arrayBuffer.toVector.toIterator)

      receivedBytesIterator.toVector shouldBe Vector(receivedBytes, receivedBytes2)
    }
  }
}
