package us.woop.pinger.data

import akka.util.ByteString
import org.scalatest.{Matchers, WordSpecLike}

class SauerBytesBinaryTest extends WordSpecLike with Matchers {
  "Received bytes binary converter" must {
    "Go forth and back" in {
      val byteString = ByteString(1, 1, 1, 5, 5, -128, 3, 1, 3, -128, -61, 1, 17,
        1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0
      )
      val receivedBytes = SauerBytes(Server("127.1.1.1", 1234), 12314, byteString.toVector)
      val byteArray = receivedBytes.toBytes

      val expectedByteArraySize = 4 + 4 + 8 + byteString.size
      byteArray.size shouldBe expectedByteArraySize

      val haveBytes = SauerBytes.fromBytes(byteArray)
      haveBytes.server.ip shouldBe receivedBytes.server.ip
      haveBytes.server.port shouldBe receivedBytes.server.port
      haveBytes.server shouldBe receivedBytes.server
      haveBytes.time shouldBe receivedBytes.time
      haveBytes.message shouldBe receivedBytes.message
      haveBytes shouldBe receivedBytes
    }
  }
}
