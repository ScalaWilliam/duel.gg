package us.woop.pinger.app

import java.io.File
import java.nio.file.Files

object H2Tests extends App {

  // http://www.h2database.com/html/mvstore.html
  import org.h2.mvstore._
  case class House(colour: String, bedrooms: Int, name: Option[String])
  val s = MVStore.open("database")
  val map = s.openMap[String, House]("Homes")
  val richardsHouse = House("RED", 3, Option("Richard"))
  map.put("Richard's house", richardsHouse)
  // s.commit() // can be good too
  s.close()

  // http://stackoverflow.com/questions/1158777/renaming-a-file-using-java
  // Files.move()
  new File("database").renameTo(new File("database-new"))

  println("Finding homes...")
  val ss = MVStore.open("database-new")
  val mm = ss.openMap[String, House]("Homes")
  import collection.JavaConverters._
  mm.asScala.foreach(println)
  assert(mm.get("Richard's house") == richardsHouse)
  ss.close()

}
