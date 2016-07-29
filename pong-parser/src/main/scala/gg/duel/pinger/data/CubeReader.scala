package gg.duel.pinger.data

import akka.util.ByteString
import gg.duel.pinger.data.PongParser.{CubeString, GetIp, UChar}

/**
  * Created by me on 10/07/2016.
  */

case class ByteStringReaderResult[T](result: T, byteStringReader: ByteStringReader)

case class ByteStringReader(data: ByteString, pos: Int, stringBuilder: java.lang.StringBuilder) {

  def seek(plus: Int) = copy(pos = pos + plus)

  def count = data.length

  def nextString(capacity: Int = 16): ByteStringReaderResult[String] = {
    stringBuilder.setLength(0)
    var ByteStringReaderResult(res, next) = nextInt()
    while (res != 0 && res != Int.MinValue) {
      stringBuilder.append(CubeString.charMapping(UChar(res.toChar)))
      nextInt() match {
        case ByteStringReaderResult(a, b) =>
          res = a
          next = b
      }
    }
    ByteStringReaderResult(stringBuilder.toString, next)
  }

  def nextIp(): ByteStringReaderResult[String] = {
    GetIp.get(data, pos).map { case (s, p) =>
      ByteStringReaderResult(s, seek(p))
    }.getOrElse(ByteStringReaderResult("", this))
  }

  def nextInt(): ByteStringReaderResult[Int] = {
    if (count <= pos) ByteStringReaderResult(Int.MinValue, this)
    else {
      val first = data(pos)
      if (first == -128 && count >= pos + 3) {
        val a = UChar(data(pos + 1))
        val b = UChar(data(pos + 2))
        val r = a | (b << 8)
        ByteStringReaderResult(r, seek(3))
      } else if (first == -127 && count >= pos + 5) {
        val m = UChar(data(pos + 1))
        val n = UChar(data(pos + 2))
        val o = UChar(data(pos + 3))
        val p = UChar(data(pos + 4))
        val r = ((m | (n << 8)) | o << 16) | (p << 24)
        ByteStringReaderResult(r, seek(5))
      } else {
        ByteStringReaderResult(first.toInt, seek(1))
      }
    }
  }
}

class CubeReader(val data: ByteString) {
  val count = data.length
  var pos = 0

  def rest: ByteString = data.drop(pos)

  val stringBuilder = new java.lang.StringBuilder()

  def nextString(capacity: Int = 16): String = {
    stringBuilder.setLength(0)
    var res = nextInt()
    while (res != 0 && res != Int.MinValue) {
      stringBuilder.append(CubeString.charMapping(UChar(res.toChar)))
      res = nextInt()
    }
    stringBuilder.toString
  }

  def nextIp(): String = {
    GetIp.get(data, pos).map(_._1).getOrElse("")
  }

  def nextInt(): Int = {
    if (count <= pos) Int.MinValue
    else {
      val first = data(pos)
      if (first == -128 && count >= pos + 3) {
        val a = UChar(data(pos + 1))
        val b = UChar(data(pos + 2))
        val r = a | (b << 8)
        pos = pos + 3
        r
      } else if (first == -127 && count >= pos + 5) {
        val m = UChar(data(pos + 1))
        val n = UChar(data(pos + 2))
        val o = UChar(data(pos + 3))
        val p = UChar(data(pos + 4))
        val r = ((m | (n << 8)) | o << 16) | (p << 24)
        pos = pos + 5
        r
      } else {
        pos = pos + 1
        first.toInt
      }
    }
  }
}
