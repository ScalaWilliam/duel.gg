//import java.io.{FileInputStream, File}
//import java.nio.{ByteBuffer, ByteOrder}
//import akka.util.ByteString
//import gg.duel.pinger.data.Extractor
//import gg.duel.pinger.data.persistence.SauerReaderWriter
//import gg.duel.pinger.service.PingPongProcessor.ReceivedBytes
//
//import scala.util.Try
//
//object ReadStoreApp extends App {
//
//
//  val fah = new File("db.14b5b49f-c82b-4f06-8c26-fda60f64978a.db")
//  val receivedBytesI = SauerReaderWriter.readFromFile(fah)
//
////  receivedBytesI map (_.message) map (x => Try(Extractor.extract(x))) filter(_.isFailure) foreach println
//  val kk = receivedBytesI map (_.message) map (x => Try(Extractor.extract(ByteString(x.toArray)))) flatMap(_.toOption.toIterator.flatMap(_.toIterator))
//  kk foreach println
//
//}
