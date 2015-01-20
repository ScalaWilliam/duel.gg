//import java.io.File
//import gg.duel.pinger.analytics.MultiplexedDuelReader
//import MultiplexedDuelReader.ImplicitDuelReader
//import gg.duel.pinger.data.persistence.SauerReaderWriter
//
//object ReadDuelsApp extends App {
//  val fah = new File("db.14b5b49f-c82b-4f06-8c26-fda60f64978a.db")
//  import ImplicitDuelReader._
//  def faf = SauerReaderWriter.readFromFile(fah)
//  val duelsA = MultiplexedDuelReader.multiplex(faf).take(20)
//  val duelsB = MultiplexedDuelReader.multiplexSecond(faf).take(20)
//  import org.scalactic.Requirements._
//  val results = duelsA.zip(duelsB).toVector
//  require(results.size == 20)
//  require(results.forall{case (a, b) => a == b})
//}
