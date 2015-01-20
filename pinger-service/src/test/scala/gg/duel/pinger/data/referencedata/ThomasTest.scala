package gg.duel.pinger.data.referencedata

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import gg.duel.pinger.data.PongParser

/** 01/02/14 */
class ThomasTest extends WordSpecLike with Matchers with BeforeAndAfterAll {
  import PongParser._
  val thomasNoHeaderInput = List(-3, 1, 0, -128, -104, 8, -128, -68, 2, 0, 2, 3, -3, 1, 0, -128, 108, 7, -128, 44, 1, 1, 3, 2, -3, 1, 0, -128, -24, 3, 100, 0, 1, 0, 66, 79, 83, 83, 0, 101, 118, 105, 108, 0, 0, 0, 2, 1, 10, 1, 0, 4, 0, 0, 84, 119, -13).map(_.toByte)
  //println(GetThomasExt.unapply(input))
  val thomasHeaderInput = List(0, 1, -1, -1, 105, 0, -3, 1, 0, 0, 0, 0, 0, 0, -3, 1, 0, -128, -124, 28, -128, 8, 7, 2, 4, 4, 101, 118, 105, 108, 0, 18, 2, 22, 0, 24, 1, 0, 4, 0, 0, 5, -110, 84).map(_.toByte)
  //println(SauerbratenProtocol.matchers.lift.apply(thmz.map(_.toByte)))
  //println(matchers(binput))

  val anotherInput = List(0, 1, -1, -1, 105, 0, -11, 16, 58, 77, 97, 108, 108, 97, 107, 107, 97, 0, 103, 111, 111, 100, 0, 0, 0, 0, 0, 0, 100, 0, 6, 0, 5, 47, 65, 13, -1, -2, 1, 0, 0, 0, 0, 0, 0, 0).map(_.toByte)
//  println(matchers(anotherInput))

  val olderClientInput = List(0, 2, -1, -1, 105).map(_.toByte)
//  println(matchers(olderClientInput))
}
