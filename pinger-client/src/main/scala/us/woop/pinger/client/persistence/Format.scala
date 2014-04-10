package us.woop.pinger.client.persistence

import java.nio.{ByteBuffer, ByteOrder}
import akka.util.ByteStringBuilder

object Format {

  case class ServerDataKey(index: Long, server: Server) {
    def toBytes: Array[Byte] = {
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      val ipBytes = server.ip.split("\\.").map {
        _.toInt.toByte
      }
      new ByteStringBuilder().putBytes("msg".getBytes).putBytes(ipBytes).putInt(server.port).putLong(index).result().toArray
    }
  }


  object DecodeServerDataKey {

    def unapply(data: Array[Byte]) =
      decodeKey(data)

    def decodeKey(key: Array[Byte]): Option[ServerDataKey] = {
      for {
        bytes <- Option(key)
        if key.length == 16 + 3
        bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN)
        start = {
          val dst = ByteBuffer.allocate(3)
          bb.get(dst.array(), 0, 3)
          new String(dst.array())
        }
        if start == "msg"
        ip = {
          val ipB = ByteBuffer.allocate(4)
          bb.get(ipB.array(), 0, 4)
          ipB.array().map {
            _.toInt & 0xFF
          }.mkString(".")
        }
        port = bb.getInt
        idx = bb.getLong
      } yield {
        ServerDataKey(idx, Server(ip, port))
      }
    }

  }

  case class Server(ip: String, port: Int) {
    def toBytes: Array[Byte] = {
      val ipBytes = ip.split("\\.").map {
        _.toInt.toByte
      }
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      new ByteStringBuilder().putBytes("server.index".getBytes).putBytes(ipBytes).putInt(port).result().toArray
    }
  }

  object ServerIndexKey {
    def unapply(key: Array[Byte]): Option[Server] =
      decodeIndexingKey(key)


    def decodeIndexingKey(key: Array[Byte]): Option[Server] = {
      val expectedFirst = "server.index"
      for {
        data <- Option(key)
        if key.length == expectedFirst.length + 4 + 4
        bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN)
        haveFirst = {
          val dst = ByteBuffer.allocate(expectedFirst.length)
          bb.get(dst.array(), 0, expectedFirst.length)
          new String(dst.array())
        }
        if expectedFirst == haveFirst
        ip = {
          val ipDst = ByteBuffer.allocate(4)
          bb.get(ipDst.array(), 0, 4)
          ipDst.array().map {
            _.toInt & 0xFF
          }.mkString(".")
        }
        port = bb.getInt
      } yield Server(ip, port)
    }

  }


  case class ServerIndexKey() {
    def toBytes: Array[Byte] = {
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      new ByteStringBuilder().putBytes("server.index".getBytes).putLong(0L).result().toArray
    }
  }

  object ServerIndexIndex {

    def unapply(data: Array[Byte]): Option[ServerIndexKey] =
      decodeMainIndexKey(data)

    def decodeMainIndexKey(key: Array[Byte]): Option[ServerIndexKey] = {
      val expected = "server.index"
      for {
        data <- Option(key)
        if key.length == expected.length + 8
        bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN)
        haveFirst = {
          val dst = ByteBuffer.allocate(expected.length)
          bb.get(dst.array(), 0, expected.length)
          new String(dst.array())
        }
        if haveFirst == expected
        nextNum = bb.getLong
        if nextNum == 0
      } yield ServerIndexKey()
    }
  }

}
