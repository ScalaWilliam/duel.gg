//import java.io.{FileWriter, FileOutputStream, File}
//import java.util.Comparator
//import java.util.function.{ToDoubleFunction, ToIntFunction, ToLongFunction, Function}
//import org.fusesource.leveldbjni.JniDBFactory._
//import org.iq80.leveldb.Options
//import us.woop.pinger.analytics.conversion.OldToNew
//import us.woop.pinger.data.journal.{MetaData, SauerBytesWriter, SauerBytes, SauerBytesBinary}
//import us.woop.pinger.data.persistence.SauerReaderWriter
//import scala.collection.mutable
//
//object OldToSortedApp extends App {
////
////  val options = new Options()
////  options.createIfMissing(false)
////  val db = factory.open(new File("***REMOVED***/Projects/26/ladder.sauer/data/19-21/"), options)
////
////
////  val options2 = new Options()
////  options2.createIfMissing(true)
////  val db2 = factory.open(new File("***REMOVED***/Projects/26/ladder.sauer/data/19-21-Out-Sorted/"), options2)
////
////  OldToNew.loadData(db).map(SauerBytesBinary.toBytes).foreach(db2.put(_, Array.empty))
////
////  db2.close()
////  db.close()
//
//  val options2 = new Options()
//  options2.createIfMissing(false)
//  val db2 = factory.open(new File("***REMOVED***/Projects/26/ladder.sauer/data/19-21-Out-Sorted/"), options2)
//  import collection.JavaConverters._
//  val it = db2.iterator()
//  it.seekToFirst()
//  val sauerBytes = it.asScala.map(_.getKey).map(SauerBytesBinary.fromBytes)
//
////  sauerBytes.sliding(2).map(x => x(1).time - x(0).time) foreach println
//
//  def grouped[T](it: Iterator[T])(n: Int): Iterator[Iterator[T]] = {
//    Iterator.continually(it.take(n))
//  }
//  grouped(sauerBytes)(5000000).foreach(buildIt)
////  val groupedIts = sauerBytes.grouped(5000000).map(_.toIterator).foreach(buildIt)
//
//  def buildIt(data: Iterator[SauerBytes]) = {
//    println("One conversion started...")
//    val first = data.next()
//    val metaData = MetaData.build.withRecordStartTime(first.time).copy(source = Option("A split conversion from banger@kay, 04-19"))
//    val outputJson = new File(s"${metaData.id}.json")
//    val os = new FileWriter(outputJson)
//    os.write(metaData.toJson)
//    os.flush()
//    os.close()
//
//    val outputLog = new File(s"${metaData.id}.log")
//    val currentStream = new FileOutputStream(outputLog)
//    val writer = SauerBytesWriter.createInjectedWriter(b => {
//      currentStream.write(b)
//      currentStream.flush()
//    })
//    writer(first)
//    data.foreach(writer)
//    currentStream.close()
//    println("One conversion complete.")
//  }
//
//}
