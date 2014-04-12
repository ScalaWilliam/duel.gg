package us.woop.pinger.data.persistence

import org.scalatest.{FlatSpec, Matchers, WordSpec}

class FormatAssertionTest extends FlatSpec with Matchers {

  import Format._


  "Server data key" should "marshall and unmarshall correctly" in {
    val key = ServerDataKey(123L, Server("123.112.123.44", 123))
    val binary = key.toBytes
    val DecodeServerDataKey(returnedKey) = binary
    returnedKey shouldBe key
  }

  "Server index key" should "marshall and unmarshall correctly" in {
    val key = Server("123.112.123.44", 123)
    val binary = key.toBytes
    val ServerIndexKey(serverKey) = binary
    serverKey shouldBe key
  }

  "Server index index" should "marshall and unmarshall correctly" in {
    val In = ServerIndexIndexKey()
    val binary = In.toBytes
    val ServerIndexIndex(ii) = binary
    ii shouldBe In
  }

}
