package us.woop.pinger.service.publish

object Tgt extends App {
  import org.json4s._
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization.{read, write}
  implicit val formats = Serialization.formats(NoTypeHints)
  val output = write(Map(("hey", "baby") -> "yay"))
  println(output)
}
