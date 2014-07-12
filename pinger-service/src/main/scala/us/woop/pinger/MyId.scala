package us.woop.pinger

import java.lang.management.ManagementFactory

import org.joda.time.format.DateTimeFormat

case class MyId(myId: String) {
  override def toString = s"$myId"
}
object MyId {
  val default = MyId{
    val procId = ManagementFactory.getRuntimeMXBean.getName.replaceAllLiterally("@", "-")
    val date = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss").print(System.currentTimeMillis)
    s"$date-$procId"
  }
}
