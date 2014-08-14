import java.io.File
import java.sql.DriverManager

import akka.util.ByteStringBuilder
import org.h2.mvstore.MVStore
import us.woop.pinger.analytics.conversion.OldToNew
import us.woop.pinger.data.SauerBytesBinary
import us.woop.pinger.data.journal.SauerBytesBinary
import us.woop.pinger.data.persistence.SauerReaderWriter

object OldToNewApp extends App {
  import org.iq80.leveldb
  import leveldb._
  import org.fusesource.leveldbjni.JniDBFactory._
  val options = new Options()
  options.createIfMissing(false)
  val db = factory.open(new File("***REMOVED***/Projects/26/ladder.sauer/19-21/"), options)
  try {
//    val testStore = MVStore.open("yaah")
//    val goma = testStore.openMap[Array[Byte], Array[Byte]]("cool")
//    goma.put()
//    import collection.JavaConverters._
//    val writer = SauerReaderWriter.writeToFile(new File("***REMOVED***/Projects/26/ladder.sauer/db-19-21.db"))
//    var c: Long = 0
//
//    val meeva = MVStore.open("Oh, good")
//    OldToNew.loadData(db) foreach {
//      data =>
//        c = c + 1
//
//        if ( c % 50000 == 0 ) {
//          println(s"Reached message $c")
//        }
//        writer.write(data)
//    }

//    val allData = OldToNew.loadData(db).toVector.sortBy(_.time)
//    val writer = SauerReaderWriter.writeToFile(new File("***REMOVED***/Projects/26/ladder.sauer/db-19-21.b.db"))
//    allData foreach writer.write
//
//
//
//    Class.forName("org.h2.Driver")
//
//    val conn = DriverManager.getConnection("jdbc:h2:~/test-load")
//
//    val stmt = conn.prepareStatement("SELECT data FROM records ORDER BY time ASC")
//
//    val resultSet = stmt.executeQuery()
//    println("Query executed... now getting data out...")
//    val records = Iterator.continually(resultSet.next).takeWhile(_ == true).map{
//      _ =>
//        resultSet.getBytes("data")
//    }
//
//    val writer = SauerReaderWriter.writeToFile(new File("***REMOVED***/Projects/26/ladder.sauer/db-19-21.c.db"))
//    records.map(SauerBytes.fromBytes).foreach(writer.write)


    val options2 = new Options()
    val db2 = factory.open(new File("***REMOVED***/Projects/26/ladder.sauer/19-21-Sorted/"), options2)
    try {
      val iterator = db2.iterator()
      iterator.seekToFirst()
      import collection.JavaConverters._
      val records = iterator.asScala.map(_.getKey).map(SauerBytesBinary.fromBytes)
      val writer = SauerReaderWriter.writeToFile(new File("***REMOVED***/Projects/26/ladder.sauer/db-19-21.d.db"))
      records.foreach(writer.write)
//      println(counts)
    } finally {
      db2.close()
    }



//    for {
//      data <- OldToNew.loadData(db)
//    } {
//      db2.put(SauerBytes2.toBytes(data), Array.empty)
//    }
//    db2.resumeCompactions()
//    db2.close()


    //    conn.prepareCall(
//      """
//        |DROP TABLE records IF EXISTS;
//      """.stripMargin).execute()
//    conn.prepareCall(
//      """CREATE TABLE
//        |records(time BIGINT, data BINARY)
//      """.stripMargin).execute()
//
//    val stmt = conn.prepareStatement("INSERT INTO records(time, data) VALUES (?, ?)")
//    for {
//      data <- OldToNew.loadData(db)
//    } {
////      println("Loading record in...")
//      stmt.setLong(1, data.time)
//      stmt.setBytes(2, data.toBytes)
//      stmt.execute()
//    }
  } finally {
    db.close()
  }

}
