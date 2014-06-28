package us.woop.pinger.data.persistence

import java.nio.{ByteBuffer, ByteOrder}
import akka.util.ByteStringBuilder

object Format {

  case class ServerDataKey(index: Long, server: Server) {
    def toBytes: Array[Byte] = {
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      val ipBytes = server.ip.split("\\.").map {
        _.toInt.toByte
      }
      assert(ipBytes.size == 4, "ipBytes must be of size 4")
      new ByteStringBuilder().putByte(2).putBytes(ipBytes).putInt(server.port).putLong(index).result().toArray
    }
  }

  object DecodeServerDataKey {

    def unapply(data: Array[Byte]) =
      decodeKey(data)

    def decodeKey(key: Array[Byte]): Option[ServerDataKey] = {
      for {
        bytes <- Option(key)
        if key.length == 17
        bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN)
        if bb.get == 2
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
      assert(ipBytes.size == 4, "ipBytes must be of size 4")
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      new ByteStringBuilder().putByte(1).putByte(1).putBytes(ipBytes).putInt(port).result().toArray
    }
  }

  object ServerIndexKey {
    def unapply(key: Array[Byte]): Option[Server] =
      decodeIndexingKey(key)

    def decodeIndexingKey(key: Array[Byte]): Option[Server] = {
      for {
        data <- Option(key)
        if key.length == 10
        bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN)
        if bb.get == 1
        if bb.get == 1
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

  case class ServerIndexIndexKey() {
    def toBytes: Array[Byte] = {
      implicit val byteOrdering = ByteOrder.BIG_ENDIAN
      new ByteStringBuilder().putByte(1).putByte(0).result().toArray
    }
  }

  object ServerIndexIndex {

    def unapply(data: Array[Byte]): Option[ServerIndexIndexKey] =
      decodeMainIndexKey(data)

    def decodeMainIndexKey(key: Array[Byte]): Option[ServerIndexIndexKey] = {
      for {
        data <- Option(key)
        if data.length == 2
        bb = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN)
        if bb.get == 1
        if bb.get == 0
      } yield ServerIndexIndexKey()
    }
  }

}
