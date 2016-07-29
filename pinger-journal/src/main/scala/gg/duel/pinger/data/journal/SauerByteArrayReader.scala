package gg.duel.pinger.data.journal

/**
 * Created by William on 27/10/2015.
 */
class SauerByteArrayReader(list: Array[Byte]) extends SauerByteReader {
  var latestList = list

  override def get(num: Int): Array[Byte] = {
    val (start, end) = latestList.splitAt(num)
    latestList = end
    if (start.length == num) start
    else null
  }

}
