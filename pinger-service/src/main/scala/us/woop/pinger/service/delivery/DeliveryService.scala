//package us.woop.pinger.service.delivery
//
//import java.io.{File, FileOutputStream}
//
//import akka.actor.{ActorRef, ActorSystem}
//import akka.stream.{MaterializerSettings, FlowMaterializer, Transformer}
//import akka.stream.scaladsl.{Duct, Flow}
//import com.hazelcast.core.{HazelcastInstance, ITopic}
//import org.reactivestreams.Publisher
//import us.BaseXPersister.PublicDuelId
//import us.woop.pinger.data.Server
//import us.woop.pinger.service.PingerController.Monitor
//import us.{WSAsyncDuelPersister, StandaloneWSAPI}
//import us.woop.pinger.analytics.DuelMaker.{SimpleCompletedDuel, CompletedDuel}
//import us.woop.pinger.analytics.MultiplexedDuelReader
//import us.woop.pinger.analytics.MultiplexedDuelReader.{SFoundGame, SInitial, SIteratorState}
//import us.woop.pinger.data.journal.{SauerBytesWriter, SauerBytes, IterationMetaData}
//import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
//
//import scala.collection.immutable
//import scala.concurrent.Await
//
//object BingaService {
//
//  def receivedBytesTransformer = new Transformer[SauerBytes, CompletedDuel] {
//    var currentState: SIteratorState = SInitial
//    override def name = "sauerBytesToCompletedDuels"
//    override def onNext(element: SauerBytes): immutable.Seq[CompletedDuel] = {
//      currentState = currentState.next(element)
//      currentState match {
//        case SFoundGame(_, completedDuel) => immutable.Seq(completedDuel)
//        case _ => immutable.Seq.empty
//      }
//    }
//  }
//
//  def buildFlow
//  (hazelcast: HazelcastInstance,
//   persister: WSAsyncDuelPersister,
//   metadatas: Publisher[IterationMetaData],
//   sauerBytes: Publisher[SauerBytes]) = {
//
//    def completedDuels: Duct[SauerBytes, CompletedDuel] =
//      Duct[SauerBytes].transform(receivedBytesTransformer)
//
//    def publishMetaToTopic(topic: ITopic[String]): Duct[IterationMetaData, Unit] =
//      Duct[IterationMetaData].map(_.toJson).map(topic.publish)
//
//    def publishDuelToTopic(topic: ITopic[String]): Duct[SimpleCompletedDuel, Unit] =
//      Duct[SimpleCompletedDuel].map(_.toJson).map(topic.publish)
//
//    def publishDuelToPersisterGetId: Duct[SimpleCompletedDuel, PublicDuelId] =
//      Duct[SimpleCompletedDuel].mapFuture(duel => persister.pushDuelTakeMeta(duel))
//
//    def attachMeta(meta: IterationMetaData): Duct[CompletedDuel, CompletedDuel] =
//      Duct[CompletedDuel].map(x => x.copy(metaId = Option(meta.id)))
//
//    Flow(metadatas).produceTo(publishMetaToTopic(hazelcast.getTopic[String]("meta-ids")).consume())
//
//    Flow(metadatas).map{
//      metaData =>
//        // metadatas
//        val (receivesSauerBytes, publishesSimpleCompletedDuels) =
//          completedDuels.map(x => x.copy(metaId = Option(metaData.id))).map(_.toSimpleCompletedDuel).build()
//
//        Flow(sauerBytes).produceTo(receivesSauerBytes)
//
//        sauerBytes.subscribe(receivesSauerBytes)
//
//        publishesSimpleCompletedDuels.subscribe(publishDuelToTopic(hazelcast.getTopic[String]("duels")).consume())
//
////        sauerBytes.
////
////        1
//    }
//
//
////    Flow(metadatas).broadcast()
//
//  }
//  def journalSauerBytes(metaData: IterationMetaData): Duct[SauerBytes, Unit] = {
//        val journalFile = new File(s"${metaData.id}.log")
//        val journal = new FileOutputStream(journalFile)
//        val writeToJournal = SauerBytesWriter.createInjectedWriter(b => {
//          journal.write(b)
//          journal.flush()
//        })
//        val duct = Duct[SauerBytes].map(writeToJournal)
//        duct.onComplete { _ =>
//          journal.close()
//          import scala.sys.process._
//          Seq("bzip2", "-k", journalFile.getCanonicalPath).run()
//        }
//        duct
//    }
//
//
////  def publishDuels(hazelcastTopic: ITopic[String], duelPersister: WSAsyncDuelPersister): Duct[SimpleCompletedDuel, Unit] =
////    Duct[SimpleCompletedDuel]
////
////  def journalBytes: Duct[SauerBytes, Flow[JournalState]] = {
////    Duct[SauerBytes].foreach(
////
////    )
////  }
////
////
////  def receivedBytesToCompleted: Duct[ReceivedBytes, CompletedDuel] =
////    Duct[ReceivedBytes].map(_.toSau.transform(receivedBytesTransformer)
//
//
////  val metadatas: Publisher[IterationMetaData] = _
//  val logMetadatas = Duct.apply[IterationMetaData].foreach { metadata =>
//    //    baseXOutput.pushMetadata(metadata)
//  }
//  val serversStuff = Duct.apply[Server].map(Monitor).foreach { monitorMessage =>
//    (null : ActorRef) ! monitorMessage
//  }
////  val receivedBytes: Publisher[ReceivedBytes] = _
//
//  sealed trait JournalState {
//    def metadata: IterationMetaData
//  }
//  case class JournalStarted(metadata: IterationMetaData)
//  case class JournalCompleted(metadata: IterationMetaData)
//  case class JournalCompressed(metadata: IterationMetaData)
//
//
//
////  val bytesToJournal = Duct.apply[]
////
////  val mostFlow = Duct.apply[IterationMetaData].foreach {
////    metadata =>
////      Flow(receivedBytes).
////  }
////  Flow(metadatas)
//}
//
//object DeliveryService {
////  val receivedBytes: Publisher[ReceivedBytes] = _
////  val sauerBytes: Publisher[SauerBytes] = _
////  val multiplexedGames: Publisher[CompletedDuel] = _
////  val metadatas: Publisher[IterationMetaData] = _
////
////  Flow(metadatas).foreach {
////    metaData =>
////      val journalFile = new File(s"${metaData.id}.log")
////      val journal = new FileOutputStream(journalFile)
////      val writeToJournal = SauerBytesWriter.createInjectedWriter(b => {
////        journal.write(b)
////        journal.flush()
////      })
////      val persistBytes = Flow(sauerBytes)
////      persistBytes foreach writeToJournal
////      persistBytes onComplete { f =>
////        journal.close()
////        import scala.sys.process._
////        Seq("bzip2", "-k", journalFile.getCanonicalPath).run()
////      }
////  }
////
////  Flow(sauerBytes).transform(new Transformer[SauerBytes, CompletedDuel] {
////    var currentState: SIteratorState = SInitial
////    override def name = "sauerBytesToCompletedDuels"
////    override def onNext(element: SauerBytes): immutable.Seq[CompletedDuel] = {
////      currentState = currentState.next(element)
////      currentState match {
////        case SFoundGame(_, completedDuel) => immutable.Seq(completedDuel)
////        case _ => immutable.Seq.empty
////      }
////    }
////  })
////
////  val cd: Publisher[CompletedDuel] = _
////
////  val ws = new StandaloneWSAPI
////  val baseXOutput = new WSAsyncDuelPersister(ws, "http://localhost:8984", "yesz", "antpquio")
////
////  import scala.concurrent.ExecutionContext.Implicits.global
////
////  // publisher with metadata
////  val scdm: Publisher[SimpleCompletedDuel] = _
////  Flow(cd).map(_.toSimpleCompletedDuel).mapFuture(d => baseXOutput.pushDuelTakeMeta(d))
////
//
//
//}
//object DSAE extends App {
//
//
//
//
//  import org.json4s._
//  import org.json4s.native.JsonMethods._
//  import org.json4s.native.Serialization._
//  implicit val mf = formats(NoTypeHints)
//
//  case class CouchItem[T](value: T)
//  case class CouchListing[T](rows: List[CouchItem[T]])
//  val yes = parse(DSE.source).extract[CouchListing[SimpleCompletedDuel]].rows.map(_.value.copy(metaId = Option("Y"))).take(5).toIterator
//
//  implicit val sissy = ActorSystem("aws")
//  implicit val settings = FlowMaterializer( MaterializerSettings())
////  val ws = new StandaloneWSAPI
////  val baseXOutput = new WSAsyncDuelPersister(ws, "http://localhost:8984", "yesz", "antpquio")
////  import scala.concurrent.ExecutionContext.Implicits.global
////  Duct.apply.consume()
//  val yesFlow = Flow(yes)
//  val dudu = Duct.apply[SimpleCompletedDuel].build()
//  import concurrent.duration._
//  Flow(dudu._2).zip(Flow(2.seconds, 2.seconds, () =>1).toPublisher()) foreach (z => println("C", z))
//  yesFlow.broadcast(dudu._1).foreach(x => println("A", x))
////  yesFlow.buffer()
////  val yesInput = yesFlow.toPublisher()
////  Flow(yesInput) foreach (x => println("A", x))
////  Flow(yesInput) foreach (x => println("B", x))
////  Flow(yesInput).mapFuture(d => baseXOutput.pushDuelTakeMeta(d)) foreach println
////  import concurrent.duration._
////  Flow(20.seconds, 5.seconds, () => "Yes").zip(yesInput) foreach println
//
//}
//object DSE {
//  val source = """{"total_rows":63,"offset":0,"rows":[
//                 |{"id":"2014-08-16T13:49:3602:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-16T13:49:3602:00::85.214.66.181:30000","_rev":"1-c4c73f5352acf19db2d88f128a06d532","simpleId":"2014-08-16T13:49:3602:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T13:49:36+02:00","startTime":1408189776986,"map":"turbine","mode":"instagib","server":"85.214.66.181:30000","players":{"Mr.TNT":{"name":"Mr.TNT","ip":"188.61.90.x","frags":0,"weapon":"rifle","fragLog":{"8":64,"4":29,"9":70,"5":39,"10":75,"6":51,"1":5,"2":14,"7":59,"3":22}},"!s]Gangler":{"name":"!s]Gangler","ip":"93.104.47.x","frags":0,"weapon":"rifle","fragLog":{"8":115,"4":57,"9":131,"5":73,"10":150,"6":88,"1":12,"2":27,"7":100,"3":41}}}}},
//                 |{"id":"2014-08-16T14:01:1502:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-16T14:01:1502:00::85.214.66.181:30000","_rev":"1-e070abacf3925c373a9cb91e66c48a10","simpleId":"2014-08-16T14:01:1502:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T14:01:15+02:00","startTime":1408190475980,"map":"complex","mode":"instagib","server":"85.214.66.181:30000","players":{"Mr.TNT":{"name":"Mr.TNT","ip":"188.61.90.x","frags":0,"weapon":"rifle","fragLog":{"8":46,"4":23,"9":50,"5":29,"10":53,"6":35,"1":5,"2":11,"7":40,"3":17}},"!s]Gangler":{"name":"!s]Gangler","ip":"93.104.47.x","frags":0,"weapon":"rifle","fragLog":{"8":89,"4":48,"9":102,"5":54,"10":117,"6":68,"1":17,"2":27,"7":80,"3":39}}}}},
//                 |{"id":"2014-08-16T14:42:3102:00::62.75.213.175:28785","key":null,"value":{"_id":"2014-08-16T14:42:3102:00::62.75.213.175:28785","_rev":"1-c93da4fcddb2d090984cabdaca72355a","simpleId":"2014-08-16T14:42:3102:00::62.75.213.175:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T14:42:31+02:00","startTime":1408192951131,"map":"turbine","mode":"instagib","server":"62.75.213.175:28785","players":{"Isley":{"name":"Isley","ip":"141.3.239.x","frags":0,"weapon":"rifle","fragLog":{"8":92,"4":50,"9":101,"5":62,"10":114,"6":69,"1":15,"2":27,"7":80,"3":39}},"AllStar":{"name":"AllStar","ip":"93.194.231.x","frags":0,"weapon":"rifle","fragLog":{"8":83,"4":45,"9":94,"5":56,"10":103,"6":66,"1":9,"2":19,"7":73,"3":30}}}}},
//                 |{"id":"2014-08-16T14:55:4902:00::62.75.213.175:28785","key":null,"value":{"_id":"2014-08-16T14:55:4902:00::62.75.213.175:28785","_rev":"1-ccd081c950e560a8f644347651beee68","simpleId":"2014-08-16T14:55:4902:00::62.75.213.175:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T14:55:49+02:00","startTime":1408193749131,"map":"elegy","mode":"efficiency","server":"62.75.213.175:28785","players":{"Isley":{"name":"Isley","ip":"141.3.239.x","frags":59,"weapon":"rocket launcher","fragLog":{"8":48,"4":24,"9":54,"5":32,"10":59,"6":37,"1":5,"2":11,"7":42,"3":18}},"AllStar":{"name":"AllStar","ip":"93.194.231.x","frags":49,"weapon":"minigun","fragLog":{"8":38,"4":20,"9":45,"5":25,"10":49,"6":30,"1":4,"2":10,"7":36,"3":15}}},"winner":"Isley"}},
//                 |{"id":"2014-08-16T15:07:1002:00::62.75.213.175:28785","key":null,"value":{"_id":"2014-08-16T15:07:1002:00::62.75.213.175:28785","_rev":"1-0a1c97eefae2df1e66760b54ea33b412","simpleId":"2014-08-16T15:07:1002:00::62.75.213.175:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T15:07:10+02:00","startTime":1408194430131,"map":"turbine","mode":"efficiency","server":"62.75.213.175:28785","players":{"Isley":{"name":"Isley","ip":"141.3.239.x","frags":0,"weapon":"minigun","fragLog":{"8":67,"4":33,"9":74,"5":40,"10":83,"6":51,"1":8,"2":17,"7":59,"3":24}},"AllStar":{"name":"AllStar","ip":"93.194.231.x","frags":0,"weapon":"minigun","fragLog":{"8":57,"4":29,"9":63,"5":36,"10":72,"6":43,"1":8,"2":15,"7":51,"3":21}}}}},
//                 |{"id":"2014-08-16T15:19:1002:00::62.75.213.175:28785","key":null,"value":{"_id":"2014-08-16T15:19:1002:00::62.75.213.175:28785","_rev":"1-f4756878d1cf1ca2e3bfdb2ddd6aa557","simpleId":"2014-08-16T15:19:1002:00::62.75.213.175:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T15:19:10+02:00","startTime":1408195150131,"map":"ot","mode":"instagib","server":"62.75.213.175:28785","players":{"Isley":{"name":"Isley","ip":"141.3.239.x","frags":0,"weapon":"rifle","fragLog":{"8":90,"4":48,"9":97,"5":57,"10":109,"6":67,"1":12,"2":23,"7":78,"3":34}},"AllStar":{"name":"AllStar","ip":"93.194.231.x","frags":0,"weapon":"rifle","fragLog":{"8":59,"4":30,"9":68,"5":37,"10":73,"6":44,"1":9,"2":18,"7":51,"3":23}}}}},
//                 |{"id":"2014-08-16T16:06:3002:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-16T16:06:3002:00::85.214.66.181:20000","_rev":"1-b0fee7cd5b3bac984385379ecff1b1b4","simpleId":"2014-08-16T16:06:3002:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T16:06:30+02:00","startTime":1408197990961,"map":"turbine","mode":"instagib","server":"85.214.66.181:20000","players":{"|>BM<|LifeLine":{"name":"|>BM<|LifeLine","ip":"87.182.42.x","frags":0,"weapon":"rifle","fragLog":{"8":41,"4":22,"9":42,"5":25,"10":49,"6":29,"1":6,"2":15,"7":37,"3":16}},"|>BM<|Partizan":{"name":"|>BM<|Partizan","ip":"94.154.107.x","frags":0,"weapon":"rifle","fragLog":{"8":54,"4":19,"9":58,"5":27,"10":67,"6":34,"1":6,"2":12,"7":45,"3":15}}}}},
//                 |{"id":"2014-08-16T16:20:4202:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-16T16:20:4202:00::85.214.66.181:20000","_rev":"1-c53b6caddf2405118879bdfa8237a848","simpleId":"2014-08-16T16:20:4202:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T16:20:42+02:00","startTime":1408198842963,"map":"complex","mode":"instagib","server":"85.214.66.181:20000","players":{"|>BM<|LifeLine":{"name":"|>BM<|LifeLine","ip":"87.182.42.x","frags":0,"weapon":"rifle","fragLog":{"8":41,"4":17,"9":45,"5":21,"10":52,"6":28,"1":5,"2":9,"7":30,"3":12}},"|>BM<|Partizan":{"name":"|>BM<|Partizan","ip":"94.154.107.x","frags":0,"weapon":"rifle","fragLog":{"8":48,"4":25,"9":54,"5":32,"10":63,"6":37,"1":5,"2":11,"7":41,"3":16}}}}},
//                 |{"id":"2014-08-16T16:43:5402:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-16T16:43:5402:00::85.214.66.181:30000","_rev":"1-a17effc85782873a075482e7a2ff9444","simpleId":"2014-08-16T16:43:5402:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T16:43:54+02:00","startTime":1408200234979,"map":"complex","mode":"instagib","server":"85.214.66.181:30000","players":{"Mr.TNT":{"name":"Mr.TNT","ip":"188.61.90.x","frags":71,"weapon":"rifle","fragLog":{"8":57,"4":25,"9":66,"5":32,"10":71,"6":39,"1":9,"2":16,"7":47,"3":22}},"ç@ù|a":{"name":"ç@ù|a","ip":"93.37.129.x","frags":67,"weapon":"rifle","fragLog":{"8":60,"4":32,"9":63,"5":41,"10":67,"6":50,"1":6,"2":16,"7":55,"3":26}}},"winner":"Mr.TNT"}},
//                 |{"id":"2014-08-16T17:09:4502:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-16T17:09:4502:00::85.214.66.181:40000","_rev":"1-68c1433e9d166095f278a62887f2a525","simpleId":"2014-08-16T17:09:4502:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T17:09:45+02:00","startTime":1408201785993,"map":"turbine","mode":"instagib","server":"85.214.66.181:40000","players":{"jay_red":{"name":"jay_red","ip":"98.252.164.x","frags":0,"weapon":"rifle","fragLog":{"8":100,"4":43,"9":121,"5":55,"10":131,"6":73,"1":10,"2":19,"7":85,"3":30}},"!s]hades":{"name":"!s]hades","ip":"71.2.145.x","frags":0,"weapon":"rifle","fragLog":{"8":84,"4":41,"9":95,"5":52,"10":101,"6":63,"1":9,"2":19,"7":72,"3":33}}}}},
//                 |{"id":"2014-08-16T17:20:5102:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-16T17:20:5102:00::85.214.66.181:40000","_rev":"1-afb08ac926d20185be8ad4f1ab0aa0d5","simpleId":"2014-08-16T17:20:5102:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T17:20:51+02:00","startTime":1408202451992,"map":"turbine","mode":"instagib","server":"85.214.66.181:40000","players":{"jay_red":{"name":"jay_red","ip":"98.252.164.x","frags":0,"weapon":"rifle","fragLog":{"8":99,"4":61,"9":105,"5":63,"10":121,"6":75,"1":11,"2":31,"7":88,"3":46}},"!s]hades":{"name":"!s]hades","ip":"71.2.145.x","frags":0,"weapon":"rifle","fragLog":{"8":93,"4":45,"9":102,"5":55,"10":118,"6":66,"1":7,"2":21,"7":77,"3":30}}}}},
//                 |{"id":"2014-08-16T17:34:3902:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-16T17:34:3902:00::85.214.66.181:40000","_rev":"1-1225669be6c0a082ec8112e504389cb1","simpleId":"2014-08-16T17:34:3902:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T17:34:39+02:00","startTime":1408203279990,"map":"turbine","mode":"efficiency","server":"85.214.66.181:40000","players":{"jay_red":{"name":"jay_red","ip":"98.252.164.x","frags":0,"weapon":"minigun","fragLog":{"8":61,"4":31,"9":69,"5":39,"10":77,"6":45,"1":7,"2":14,"7":53,"3":22}},"!s]hades":{"name":"!s]hades","ip":"71.2.145.x","frags":0,"weapon":"minigun","fragLog":{"8":72,"4":37,"9":79,"5":45,"10":86,"6":54,"1":10,"2":18,"7":63,"3":27}}}}},
//                 |{"id":"2014-08-16T18:39:4802:00::109.73.51.58:28785","key":null,"value":{"_id":"2014-08-16T18:39:4802:00::109.73.51.58:28785","_rev":"1-05c74b13dac422b35652c83978981ff2","simpleId":"2014-08-16T18:39:4802:00::109.73.51.58:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T18:39:48+02:00","startTime":1408207188846,"map":"ot","mode":"efficiency","server":"109.73.51.58:28785","players":{"xlr8":{"name":"xlr8","ip":"84.152.209.x","frags":0,"weapon":"minigun","fragLog":{"8":59,"4":30,"9":69,"5":37,"10":77,"6":45,"1":8,"2":16,"7":53,"3":23}},"w00p|Art":{"name":"w00p|Art","ip":"146.255.147.x","frags":0,"weapon":"minigun","fragLog":{"8":53,"4":28,"9":61,"5":37,"10":68,"6":43,"1":7,"2":14,"7":49,"3":22}}}}},
//                 |{"id":"2014-08-16T18:52:1202:00::109.73.51.58:28785","key":null,"value":{"_id":"2014-08-16T18:52:1202:00::109.73.51.58:28785","_rev":"1-1a6f188abf91b1e08a173758433bff2a","simpleId":"2014-08-16T18:52:1202:00::109.73.51.58:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T18:52:12+02:00","startTime":1408207932857,"map":"turbine","mode":"efficiency","server":"109.73.51.58:28785","players":{"xlr8":{"name":"xlr8","ip":"84.152.209.x","frags":0,"weapon":"minigun","fragLog":{"8":72,"4":35,"9":82,"5":46,"10":91,"6":54,"1":8,"2":15,"7":63,"3":25}},"w00p|Art":{"name":"w00p|Art","ip":"146.255.147.x","frags":0,"weapon":"minigun","fragLog":{"8":72,"4":37,"9":80,"5":46,"10":87,"6":55,"1":9,"2":17,"7":63,"3":27}}}}},
//                 |{"id":"2014-08-16T18:59:3902:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-16T18:59:3902:00::85.214.66.181:20000","_rev":"1-73a653b5fe8d813a00eddd327734abc7","simpleId":"2014-08-16T18:59:3902:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T18:59:39+02:00","startTime":1408208379962,"map":"turbine","mode":"instagib","server":"85.214.66.181:20000","players":{"ç@ù|a":{"name":"ç@ù|a","ip":"93.37.129.x","frags":59,"weapon":"rifle","fragLog":{"8":45,"4":27,"9":49,"5":30,"10":59,"6":35,"1":9,"2":14,"7":42,"3":21}},"chaos|sp4nk":{"name":"chaos|sp4nk","ip":"50.177.91.x","frags":115,"weapon":"rifle","fragLog":{"8":97,"4":48,"9":104,"5":57,"10":115,"6":71,"1":17,"2":25,"7":85,"3":36}}},"winner":"ç@ù|a"}},
//                 |{"id":"2014-08-16T22:09:1902:00::108.61.210.80:28785","key":null,"value":{"_id":"2014-08-16T22:09:1902:00::108.61.210.80:28785","_rev":"1-c13a5a072f9856778ac9b98158ae7617","simpleId":"2014-08-16T22:09:1902:00::108.61.210.80:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T22:09:19+02:00","startTime":1408219759109,"map":"memento","mode":"efficiency","server":"108.61.210.80:28785","players":{"w00p|Art":{"name":"w00p|Art","ip":"146.255.147.x","frags":0,"weapon":"minigun","fragLog":{"8":48,"4":24,"9":54,"5":31,"10":61,"6":35,"1":7,"2":13,"7":41,"3":17}},"swattlellama":{"name":"swattlellama","ip":"75.161.133.x","frags":0,"weapon":"minigun","fragLog":{"8":44,"4":20,"9":51,"5":27,"10":57,"6":32,"1":4,"2":10,"7":38,"3":15}}}}},
//                 |{"id":"2014-08-16T22:19:4602:00::108.61.210.80:28785","key":null,"value":{"_id":"2014-08-16T22:19:4602:00::108.61.210.80:28785","_rev":"1-accdf9fa158b1fee4621e3c2900c2815","simpleId":"2014-08-16T22:19:4602:00::108.61.210.80:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T22:19:46+02:00","startTime":1408220386116,"map":"turbine","mode":"efficiency","server":"108.61.210.80:28785","players":{"w00p|Art":{"name":"w00p|Art","ip":"146.255.147.x","frags":0,"weapon":"minigun","fragLog":{"8":64,"4":32,"9":73,"5":39,"10":79,"6":48,"1":9,"2":16,"7":57,"3":23}},"swattlellama":{"name":"swattlellama","ip":"75.161.133.x","frags":0,"weapon":"minigun","fragLog":{"8":57,"4":29,"9":64,"5":36,"10":69,"6":43,"1":7,"2":14,"7":50,"3":21}}}}},
//                 |{"id":"2014-08-16T23:22:1802:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-16T23:22:1802:00::85.214.66.181:20000","_rev":"1-aa6e25ced783b63b6628dfab433e4364","simpleId":"2014-08-16T23:22:1802:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T23:22:18+02:00","startTime":1408224138961,"map":"complex","mode":"instagib","server":"85.214.66.181:20000","players":{"vaQ'sexyy":{"name":"vaQ'sexyy","ip":"68.150.220.x","frags":0,"weapon":"rifle","fragLog":{"8":84,"4":38,"9":94,"5":49,"10":107,"6":56,"1":11,"2":17,"7":69,"3":32}},"noVI.hottie":{"name":"noVI.hottie","ip":"91.234.197.x","frags":0,"weapon":"rifle","fragLog":{"8":54,"4":29,"9":59,"5":35,"10":62,"6":42,"1":8,"2":16,"7":49,"3":22}}}}},
//                 |{"id":"2014-08-16T23:32:5102:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-16T23:32:5102:00::85.214.66.181:20000","_rev":"1-c9a6569d0caf66d65c9168023c8c9a68","simpleId":"2014-08-16T23:32:5102:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T23:32:51+02:00","startTime":1408224771967,"map":"complex","mode":"efficiency","server":"85.214.66.181:20000","players":{"vaQ'sexyy":{"name":"vaQ'sexyy","ip":"68.150.220.x","frags":63,"weapon":"minigun","fragLog":{"8":52,"4":26,"9":58,"5":31,"10":63,"6":39,"1":5,"2":11,"7":46,"3":18}},"noVI.hottie":{"name":"noVI.hottie","ip":"91.234.197.x","frags":65,"weapon":"rocket launcher","fragLog":{"8":53,"4":27,"9":59,"5":34,"10":65,"6":41,"1":7,"2":14,"7":48,"3":19}}},"winner":"noVI.hottie"}},
//                 |{"id":"2014-08-16T23:52:0602:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-16T23:52:0602:00::85.214.66.181:20000","_rev":"1-8c95160db780c7528b362f4299513131","simpleId":"2014-08-16T23:52:0602:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-16T23:52:06+02:00","startTime":1408225926961,"map":"turbine","mode":"efficiency","server":"85.214.66.181:20000","players":{"noVI.hottie":{"name":"noVI.hottie","ip":"91.234.197.x","frags":55,"weapon":"rocket launcher","fragLog":{"8":42,"4":20,"9":49,"5":27,"10":55,"6":32,"1":5,"2":10,"7":37,"3":15}},"vaQ'sexyy":{"name":"vaQ'sexyy","ip":"68.150.220.x","frags":72,"weapon":"minigun","fragLog":{"8":54,"4":27,"9":65,"5":35,"10":72,"6":41,"1":8,"2":16,"7":46,"3":21}}},"winner":"noVI.hottie"}},
//                 |{"id":"2014-08-17T00:18:2402:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T00:18:2402:00::85.214.66.181:20000","_rev":"1-cbbbda0449fce4128f26ec17fdbaaff0","simpleId":"2014-08-17T00:18:2402:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T00:18:24+02:00","startTime":1408227504961,"map":"douze","mode":"ffa","server":"85.214.66.181:20000","players":{"noVI.hottie":{"name":"noVI.hottie","ip":"91.234.197.x","frags":19,"weapon":"rocket launcher","fragLog":{"8":12,"4":4,"9":17,"5":6,"10":19,"6":7,"1":0,"2":2,"7":10,"3":3}},"vaQ'sexyy":{"name":"vaQ'sexyy","ip":"68.150.220.x","frags":43,"weapon":"pistol","fragLog":{"8":33,"4":19,"9":39,"5":24,"10":42,"6":28,"1":5,"2":9,"7":31,"3":14}}},"winner":"vaQ'sexyy"}},
//                 |{"id":"2014-08-17T01:02:4502:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T01:02:4502:00::85.214.66.181:20000","_rev":"1-c1b05c31d40950cf0730341399094421","simpleId":"2014-08-17T01:02:4502:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T01:02:45+02:00","startTime":1408230165961,"map":"turbine","mode":"instagib","server":"85.214.66.181:20000","players":{"ç@ù|a":{"name":"ç@ù|a","ip":"93.37.129.x","frags":0,"weapon":"rifle","fragLog":{"8":59,"4":32,"9":66,"5":39,"10":77,"6":45,"1":7,"2":14,"7":51,"3":23}},"seelfmade":{"name":"seelfmade","ip":"213.187.69.x","frags":0,"weapon":"rifle","fragLog":{"8":115,"4":53,"9":131,"5":70,"10":146,"6":83,"1":10,"2":22,"7":94,"3":36}}}}},
//                 |{"id":"2014-08-17T01:13:3602:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T01:13:3602:00::85.214.66.181:20000","_rev":"1-98c5d6440347980582419ab513c95154","simpleId":"2014-08-17T01:13:3602:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T01:13:36+02:00","startTime":1408230816959,"map":"complex","mode":"instagib","server":"85.214.66.181:20000","players":{"ç@ù|a":{"name":"ç@ù|a","ip":"93.37.129.x","frags":84,"weapon":"rifle","fragLog":{"8":68,"4":31,"9":76,"5":40,"10":83,"6":50,"1":6,"2":12,"7":60,"3":20}},"seelfmade":{"name":"seelfmade","ip":"213.187.69.x","frags":114,"weapon":"rifle","fragLog":{"8":88,"4":46,"9":101,"5":55,"10":112,"6":65,"1":11,"2":21,"7":74,"3":35}}},"winner":"seelfmade"}},
//                 |{"id":"2014-08-17T01:23:4502:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T01:23:4502:00::85.214.66.181:20000","_rev":"1-fba1a2d6e8e7db59d128d2ae0d8856e5","simpleId":"2014-08-17T01:23:4502:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T01:23:45+02:00","startTime":1408231425966,"map":"aard3c","mode":"instagib","server":"85.214.66.181:20000","players":{"ç@ù|a":{"name":"ç@ù|a","ip":"93.37.129.x","frags":0,"weapon":"rifle","fragLog":{"8":-2,"4":-2,"9":-3,"5":-2,"10":-3,"6":-2,"1":0,"2":0,"7":-2,"3":-2}},"seelfmade":{"name":"seelfmade","ip":"213.187.69.x","frags":0,"weapon":"rifle","fragLog":{"8":1,"4":-1,"9":1,"5":0,"10":0,"6":0,"1":0,"2":0,"7":0,"3":0}}}}},
//                 |{"id":"2014-08-17T02:21:0302:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T02:21:0302:00::85.214.66.181:20000","_rev":"1-ecc1e7fd3a2f6b2638828ed8bf398337","simpleId":"2014-08-17T02:21:0302:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T02:21:03+02:00","startTime":1408234863961,"map":"turbine","mode":"instagib","server":"85.214.66.181:20000","players":{".rC|Jawer":{"name":".rC|Jawer","ip":"91.58.234.x","frags":87,"weapon":"rifle","fragLog":{"8":68,"4":32,"9":78,"5":39,"10":87,"6":49,"1":5,"2":10,"7":59,"3":24}},"vzz3c|starless":{"name":"vzz3c|starless","ip":"91.56.95.x","frags":106,"weapon":"rifle","fragLog":{"8":85,"4":38,"9":93,"5":48,"10":106,"6":60,"1":9,"2":21,"7":75,"3":31}}},"winner":"vzz3c|starless"}},
//                 |{"id":"2014-08-17T02:37:4502:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T02:37:4502:00::85.214.66.181:20000","_rev":"1-3d85e44135a9871ac902f3f354de2911","simpleId":"2014-08-17T02:37:4502:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T02:37:45+02:00","startTime":1408235865963,"map":"park","mode":"instagib","server":"85.214.66.181:20000","players":{".rC|Jawer":{"name":".rC|Jawer","ip":"91.58.234.x","frags":0,"weapon":"rifle","fragLog":{"8":43,"4":21,"9":49,"5":25,"10":58,"6":31,"1":2,"2":10,"7":39,"3":16}},"vzz3c|starless":{"name":"vzz3c|starless","ip":"91.56.95.x","frags":0,"weapon":"rifle","fragLog":{"8":47,"4":25,"9":53,"5":30,"10":58,"6":35,"1":7,"2":14,"7":44,"3":19}}}}},
//                 |{"id":"2014-08-17T03:34:2702:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T03:34:2702:00::85.214.66.181:20000","_rev":"1-d7bfe6ef2d487f325405633665f4aa8b","simpleId":"2014-08-17T03:34:2702:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T03:34:27+02:00","startTime":1408239267961,"map":"turbine","mode":"efficiency","server":"85.214.66.181:20000","players":{"!s]Nighty":{"name":"!s]Nighty","ip":"71.2.145.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":48,"4":20,"9":55,"5":29,"10":60,"6":34,"1":6,"2":9,"7":41,"3":16}},"!s]Fatality":{"name":"!s]Fatality","ip":"96.238.130.x","frags":0,"weapon":"rifle","fragLog":{"8":40,"4":19,"9":45,"5":25,"10":51,"6":29,"1":7,"2":11,"7":34,"3":16}}}}},
//                 |{"id":"2014-08-17T04:04:0902:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T04:04:0902:00::85.214.66.181:30000","_rev":"1-573085ae051cf689a1ae8b77fd385192","simpleId":"2014-08-17T04:04:0902:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T04:04:09+02:00","startTime":1408241049983,"map":"ladder","mode":"efficiency","server":"85.214.66.181:30000","players":{"!s]Nighty":{"name":"!s]Nighty","ip":"71.2.145.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":58,"4":35,"9":60,"5":42,"10":64,"6":48,"1":7,"2":17,"7":54,"3":29}},"!s]Fatality":{"name":"!s]Fatality","ip":"96.238.130.x","frags":0,"weapon":"rifle","fragLog":{"8":55,"4":32,"9":60,"5":37,"10":66,"6":42,"1":9,"2":19,"7":47,"3":27}}}}},
//                 |{"id":"2014-08-17T04:29:1502:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T04:29:1502:00::85.214.66.181:30000","_rev":"1-21d175b4c836a956588074bbb2d68cf8","simpleId":"2014-08-17T04:29:1502:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T04:29:15+02:00","startTime":1408242555985,"map":"turbine","mode":"efficiency","server":"85.214.66.181:30000","players":{"!s]Nighty":{"name":"!s]Nighty","ip":"71.2.145.x","frags":0,"weapon":"minigun","fragLog":{"8":45,"4":21,"9":53,"5":27,"10":58,"6":32,"1":6,"2":13,"7":38,"3":17}},"demannu":{"name":"demannu","ip":"75.164.165.x","frags":0,"weapon":"minigun","fragLog":{"8":71,"4":28,"9":84,"5":39,"10":95,"6":48,"1":9,"2":19,"7":60,"3":24}}}}},
//                 |{"id":"2014-08-17T04:32:4202:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T04:32:4202:00::85.214.66.181:20000","_rev":"1-314d460c98dd0c44c06ed26618d6b78f","simpleId":"2014-08-17T04:32:4202:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T04:32:42+02:00","startTime":1408242762961,"map":"metl2","mode":"ffa","server":"85.214.66.181:20000","players":{"[tBMC]swatllama":{"name":"[tBMC]swatllama","ip":"75.161.133.x","frags":29,"weapon":"rocket launcher","fragLog":{"8":22,"4":10,"9":25,"5":12,"10":29,"6":13,"1":5,"2":6,"7":18,"3":8}},"nature":{"name":"nature","ip":"77.194.89.x","frags":24,"weapon":"rocket launcher","fragLog":{"8":20,"4":7,"9":22,"5":13,"10":24,"6":18,"1":3,"2":3,"7":19,"3":7}}},"winner":"[tBMC]swatllama"}},
//                 |{"id":"2014-08-17T04:43:2702:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T04:43:2702:00::85.214.66.181:20000","_rev":"1-cd44ecbc96d75cc6eaef30a7fe59d3b3","simpleId":"2014-08-17T04:43:2702:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T04:43:27+02:00","startTime":1408243407963,"map":"metl3","mode":"ffa","server":"85.214.66.181:20000","players":{"[tBMC]swatllama":{"name":"[tBMC]swatllama","ip":"75.161.133.x","frags":29,"weapon":"minigun","fragLog":{"8":21,"4":8,"9":25,"5":12,"10":29,"6":16,"1":0,"2":0,"7":17,"3":6}},"nature":{"name":"nature","ip":"77.194.89.x","frags":26,"weapon":"minigun","fragLog":{"8":25,"4":17,"9":25,"5":19,"10":26,"6":20,"1":4,"2":10,"7":21,"3":12}}},"winner":"nature"}},
//                 |{"id":"2014-08-17T04:44:0602:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T04:44:0602:00::85.214.66.181:30000","_rev":"1-e2a5324be45ec496ccd841b14a67c703","simpleId":"2014-08-17T04:44:0602:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T04:44:06+02:00","startTime":1408243446983,"map":"deathtek","mode":"efficiency","server":"85.214.66.181:30000","players":{"!s]Nighty":{"name":"!s]Nighty","ip":"71.2.145.x","frags":0,"weapon":"rifle","fragLog":{"8":13,"4":6,"9":16,"5":7,"10":17,"6":10,"1":0,"2":2,"7":11,"3":4}},"demannu":{"name":"demannu","ip":"75.164.165.x","frags":0,"weapon":"minigun","fragLog":{"8":30,"4":12,"9":35,"5":18,"10":40,"6":24,"1":1,"2":3,"7":27,"3":10}}}}},
//                 |{"id":"2014-08-17T04:54:1202:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T04:54:1202:00::85.214.66.181:30000","_rev":"1-74185ba37d037d312a5f1a0d058320b9","simpleId":"2014-08-17T04:54:1202:00::85.214.66.181:30000","duration":9,"playedAt":[5,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T04:54:12+02:00","startTime":1408244052982,"map":"depot","mode":"efficiency","server":"85.214.66.181:30000","players":{"!s]Nighty":{"name":"!s]Nighty","ip":"71.2.145.x","frags":0,"weapon":"rifle","fragLog":{"8":1,"4":-1,"9":0,"5":-3,"6":-2,"1":0,"2":0,"7":-1,"3":0}},"demannu":{"name":"demannu","ip":"75.164.165.x","frags":0,"weapon":"rifle","fragLog":{"8":8,"4":3,"9":0,"5":4,"6":5,"1":2,"2":2,"7":6,"3":3}}}}},
//                 |{"id":"2014-08-17T05:02:3302:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T05:02:3302:00::85.214.66.181:30000","_rev":"1-499d262215afe5dcc6a80dbc3600faa1","simpleId":"2014-08-17T05:02:3302:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T05:02:33+02:00","startTime":1408244553983,"map":"tartech","mode":"efficiency","server":"85.214.66.181:30000","players":{"!s]Nighty":{"name":"!s]Nighty","ip":"71.2.145.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":26,"4":13,"9":30,"5":16,"10":33,"6":19,"1":3,"2":6,"7":22,"3":10}},"demannu":{"name":"demannu","ip":"75.164.165.x","frags":0,"weapon":"minigun","fragLog":{"8":46,"4":24,"9":53,"5":30,"10":58,"6":34,"1":5,"2":10,"7":41,"3":17}}}}},
//                 |{"id":"2014-08-17T09:21:4802:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T09:21:4802:00::85.214.66.181:20000","_rev":"1-18699c4b619bc9ce25ee7b59943dac31","simpleId":"2014-08-17T09:21:4802:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T09:21:48+02:00","startTime":1408260108963,"map":"turbine","mode":"efficiency","server":"85.214.66.181:20000","players":{"!s]starch":{"name":"!s]starch","ip":"75.164.165.x","frags":0,"weapon":"minigun","fragLog":{"8":23,"4":3,"9":33,"5":7,"10":41,"6":5,"1":2,"2":2,"7":16,"3":3}},"hades":{"name":"hades","ip":"71.2.145.x","frags":0,"weapon":"minigun","fragLog":{"8":16,"4":4,"9":23,"5":6,"10":30,"6":3,"1":1,"2":1,"7":10,"3":3}}}}},
//                 |{"id":"2014-08-17T09:39:4802:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T09:39:4802:00::85.214.66.181:20000","_rev":"1-e6eb7d369534bad77bccb8efb866a545","simpleId":"2014-08-17T09:39:4802:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T09:39:48+02:00","startTime":1408261188965,"map":"turbine","mode":"efficiency","server":"85.214.66.181:20000","players":{"!s]starch":{"name":"!s]starch","ip":"75.164.165.x","frags":104,"weapon":"minigun","fragLog":{"8":82,"4":41,"9":92,"5":53,"10":104,"6":65,"1":12,"2":23,"7":73,"3":31}},"hades":{"name":"hades","ip":"71.2.145.x","frags":76,"weapon":"minigun","fragLog":{"8":62,"4":34,"9":68,"5":42,"10":76,"6":49,"1":8,"2":18,"7":53,"3":26}}},"winner":"!s]starch"}},
//                 |{"id":"2014-08-17T10:25:5402:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T10:25:5402:00::85.214.66.181:40000","_rev":"1-bc858c1948b3d748a82c68737bddb4ec","simpleId":"2014-08-17T10:25:5402:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T10:25:54+02:00","startTime":1408263954989,"map":"ot","mode":"efficiency","server":"85.214.66.181:40000","players":{"[tBMC]swatllama":{"name":"[tBMC]swatllama","ip":"75.161.133.x","frags":60,"weapon":"minigun","fragLog":{"8":45,"4":22,"9":52,"5":28,"10":58,"6":35,"1":6,"2":11,"7":40,"3":17}},"hades":{"name":"hades","ip":"71.2.145.x","frags":49,"weapon":"minigun","fragLog":{"8":38,"4":19,"9":44,"5":24,"10":48,"6":29,"1":6,"2":8,"7":34,"3":13}}},"winner":"hades"}},
//                 |{"id":"2014-08-17T10:36:5702:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T10:36:5702:00::85.214.66.181:40000","_rev":"1-855ce7da64686e0a842b85a282205ae8","simpleId":"2014-08-17T10:36:5702:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T10:36:57+02:00","startTime":1408264617992,"map":"turbine","mode":"efficiency","server":"85.214.66.181:40000","players":{"[tBMC]swatllama":{"name":"[tBMC]swatllama","ip":"75.161.133.x","frags":0,"weapon":"minigun","fragLog":{"8":67,"4":32,"9":74,"5":40,"10":83,"6":48,"1":9,"2":17,"7":57,"3":25}},"hades":{"name":"hades","ip":"71.2.145.x","frags":0,"weapon":"minigun","fragLog":{"8":55,"4":27,"9":62,"5":34,"10":70,"6":40,"1":9,"2":14,"7":46,"3":20}}}}},
//                 |{"id":"2014-08-17T11:03:1802:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T11:03:1802:00::85.214.66.181:30000","_rev":"1-3a963178fa4e9ed90970a97b90a7f375","simpleId":"2014-08-17T11:03:1802:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T11:03:18+02:00","startTime":1408266198981,"map":"turbine","mode":"efficiency","server":"85.214.66.181:30000","players":{"zaycev":{"name":"zaycev","ip":"93.218.50.x","frags":58,"weapon":"minigun","fragLog":{"8":43,"4":18,"9":50,"5":23,"10":58,"6":28,"1":5,"2":10,"7":35,"3":14}},"|RB|degrave":{"name":"|RB|degrave","ip":"92.101.168.x","frags":73,"weapon":"minigun","fragLog":{"8":56,"4":27,"9":64,"5":34,"10":73,"6":40,"1":8,"2":11,"7":48,"3":21}}},"winner":"|RB|degrave"}},
//                 |{"id":"2014-08-17T13:28:2702:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T13:28:2702:00::85.214.66.181:40000","_rev":"1-a52c2224304830466d6174e5b69adeeb","simpleId":"2014-08-17T13:28:2702:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T13:28:27+02:00","startTime":1408274907990,"map":"turbine","mode":"efficiency","server":"85.214.66.181:40000","players":{"w00p|fx":{"name":"w00p|fx","ip":"84.130.196.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":55,"4":26,"9":59,"5":31,"10":64,"6":38,"1":6,"2":12,"7":47,"3":20}},"?":{"name":"?","ip":"188.97.167.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":42,"4":19,"9":49,"5":23,"10":56,"6":30,"1":5,"2":8,"7":36,"3":14}}}}},
//                 |{"id":"2014-08-17T13:42:5102:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T13:42:5102:00::85.214.66.181:40000","_rev":"1-bae379a73dcf07d33ac2484639daa552","simpleId":"2014-08-17T13:42:5102:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T13:42:51+02:00","startTime":1408275771993,"map":"turbine","mode":"instagib","server":"85.214.66.181:40000","players":{"w00p|fx":{"name":"w00p|fx","ip":"84.130.196.x","frags":90,"weapon":"rifle","fragLog":{"8":72,"4":37,"9":79,"5":46,"10":90,"6":54,"1":11,"2":22,"7":61,"3":27}},"?":{"name":"?","ip":"188.97.167.x","frags":86,"weapon":"rifle","fragLog":{"8":73,"4":36,"9":79,"5":42,"10":86,"6":58,"1":7,"2":16,"7":67,"3":25}}},"winner":"?"}},
//                 |{"id":"2014-08-17T13:44:2102:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T13:44:2102:00::85.214.66.181:30000","_rev":"1-e51bda038246d5e5117c98d30dbaabf3","simpleId":"2014-08-17T13:44:2102:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T13:44:21+02:00","startTime":1408275861984,"map":"turbine","mode":"efficiency","server":"85.214.66.181:30000","players":{"!s]4t4K4n":{"name":"!s]4t4K4n","ip":"95.15.76.x","frags":0,"weapon":"minigun","fragLog":{"8":59,"4":25,"9":68,"5":34,"10":77,"6":40,"1":4,"2":12,"7":49,"3":21}},"hades":{"name":"hades","ip":"71.2.145.x","frags":0,"weapon":"minigun","fragLog":{"8":54,"4":25,"9":64,"5":32,"10":72,"6":40,"1":4,"2":10,"7":48,"3":19}}}}},
//                 |{"id":"2014-08-17T13:54:1502:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T13:54:1502:00::85.214.66.181:40000","_rev":"1-b57f069339879714123e8ea156a50597","simpleId":"2014-08-17T13:54:1502:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T13:54:15+02:00","startTime":1408276455990,"map":"duel7","mode":"efficiency","server":"85.214.66.181:40000","players":{"w00p|fx":{"name":"w00p|fx","ip":"84.130.196.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":42,"4":23,"9":44,"5":29,"10":50,"6":33,"1":6,"2":10,"7":39,"3":17}},"?":{"name":"?","ip":"188.97.167.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":41,"4":22,"9":45,"5":27,"10":51,"6":30,"1":6,"2":12,"7":37,"3":18}}}}},
//                 |{"id":"2014-08-17T13:57:1502:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T13:57:1502:00::85.214.66.181:30000","_rev":"1-7e2a019b9816166a9dd2feee8ac6c636","simpleId":"2014-08-17T13:57:1502:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T13:57:15+02:00","startTime":1408276635988,"map":"turbine","mode":"instagib","server":"85.214.66.181:30000","players":{"!s]4t4K4n":{"name":"!s]4t4K4n","ip":"95.15.76.x","frags":93,"weapon":"rifle","fragLog":{"8":77,"4":43,"9":79,"5":51,"10":87,"6":62,"1":14,"2":23,"7":69,"3":31}},"hades":{"name":"hades","ip":"71.2.145.x","frags":87,"weapon":"rifle","fragLog":{"8":75,"4":32,"9":78,"5":43,"10":83,"6":58,"1":10,"2":19,"7":65,"3":29}}},"winner":"hades"}},
//                 |{"id":"2014-08-17T15:29:4802:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T15:29:4802:00::85.214.66.181:20000","_rev":"1-e4b9fb2f81f33f1a6f3dc47580bd4eca","simpleId":"2014-08-17T15:29:4802:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T15:29:48+02:00","startTime":1408282188960,"map":"turbine","mode":"efficiency","server":"85.214.66.181:20000","players":{"hu":{"name":"hu","ip":"84.130.196.x","frags":86,"weapon":"rocket launcher","fragLog":{"8":67,"4":28,"9":73,"5":38,"10":84,"6":47,"1":7,"2":12,"7":58,"3":19}},"wp.T4m!n0":{"name":"wp.T4m!n0","ip":"93.218.50.x","frags":77,"weapon":"minigun","fragLog":{"8":62,"4":29,"9":68,"5":36,"10":75,"6":44,"1":7,"2":14,"7":51,"3":20}}},"winner":"wp.T4m!n0"}},
//                 |{"id":"2014-08-17T15:41:3002:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T15:41:3002:00::85.214.66.181:20000","_rev":"1-7593270caa1f75244a6b43dbcc83c170","simpleId":"2014-08-17T15:41:3002:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T15:41:30+02:00","startTime":1408282890960,"map":"complex","mode":"efficiency","server":"85.214.66.181:20000","players":{"hu":{"name":"hu","ip":"84.130.196.x","frags":78,"weapon":"minigun","fragLog":{"8":64,"4":34,"9":73,"5":42,"10":78,"6":49,"1":8,"2":16,"7":55,"3":26}},"wp.T4m!n0":{"name":"wp.T4m!n0","ip":"93.218.50.x","frags":75,"weapon":"minigun","fragLog":{"8":61,"4":33,"9":68,"5":41,"10":75,"6":46,"1":7,"2":16,"7":53,"3":25}}},"winner":"hu"}},
//                 |{"id":"2014-08-17T16:16:4502:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T16:16:4502:00::85.214.66.181:30000","_rev":"1-9241c7985dfbe746db41b4eefa8a6556","simpleId":"2014-08-17T16:16:4502:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T16:16:45+02:00","startTime":1408285005982,"map":"turbine","mode":"instagib","server":"85.214.66.181:30000","players":{"TylerDurden":{"name":"TylerDurden","ip":"86.101.197.x","frags":91,"weapon":"rifle","fragLog":{"8":77,"4":39,"9":83,"5":48,"10":91,"6":59,"1":8,"2":19,"7":65,"3":30}},".&.!#":{"name":".&.!#","ip":"93.37.113.x","frags":88,"weapon":"rifle","fragLog":{"8":67,"4":30,"9":77,"5":38,"10":86,"6":46,"1":8,"2":19,"7":58,"3":24}}},"winner":"TylerDurden"}},
//                 |{"id":"2014-08-17T16:28:3002:00::85.214.66.181:30000","key":null,"value":{"_id":"2014-08-17T16:28:3002:00::85.214.66.181:30000","_rev":"1-5a8ae7057e2b473d93604f3accf9b417","simpleId":"2014-08-17T16:28:3002:00::85.214.66.181:30000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T16:28:30+02:00","startTime":1408285710982,"map":"complex","mode":"instagib","server":"85.214.66.181:30000","players":{"TylerDurden":{"name":"TylerDurden","ip":"86.101.197.x","frags":0,"weapon":"rifle","fragLog":{"8":63,"4":35,"9":68,"5":44,"10":78,"6":53,"1":8,"2":21,"7":59,"3":32}},".&.!#":{"name":".&.!#","ip":"93.37.113.x","frags":0,"weapon":"rifle","fragLog":{"8":55,"4":26,"9":62,"5":35,"10":70,"6":41,"1":6,"2":14,"7":47,"3":19}}}}},
//                 |{"id":"2014-08-17T17:02:2402:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T17:02:2402:00::85.214.66.181:40000","_rev":"1-2ae89fa395d2c5f182a116e7601eb9bd","simpleId":"2014-08-17T17:02:2402:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,3,4],"startTimeText":"2014-08-17T17:02:24+02:00","startTime":1408287744989,"map":"duel7","mode":"instagib","server":"85.214.66.181:40000","players":{"IxcE":{"name":"IxcE","ip":"50.96.86.x","frags":0,"weapon":"rifle","fragLog":{"8":12,"4":7,"9":13,"5":10,"10":14,"6":12,"1":1,"2":4,"7":12,"3":5}},"Astoreth":{"name":"Astoreth","ip":"50.96.86.x","frags":0,"weapon":"rifle","fragLog":{"8":46,"4":32,"9":49,"5":40,"10":53,"6":46,"1":9,"2":18,"7":46,"3":25}}}}},
//                 |{"id":"2014-08-17T17:03:5702:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T17:03:5702:00::85.214.66.181:20000","_rev":"1-66f9ad9dacd10e2de92c477c9081eee6","simpleId":"2014-08-17T17:03:5702:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T17:03:57+02:00","startTime":1408287837964,"map":"turbine","mode":"efficiency","server":"85.214.66.181:20000","players":{"wp.sparta":{"name":"wp.sparta","ip":"186.176.73.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":57,"4":26,"9":66,"5":35,"10":73,"6":41,"1":6,"2":13,"7":49,"3":21}},".rC|Jawer":{"name":".rC|Jawer","ip":"91.58.236.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":57,"4":26,"9":66,"5":34,"10":73,"6":41,"1":7,"2":13,"7":49,"3":22}}}}},
//                 |{"id":"2014-08-17T17:17:2102:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T17:17:2102:00::85.214.66.181:40000","_rev":"1-3045ca289d852fee27668a5a95f29943","simpleId":"2014-08-17T17:17:2102:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T17:17:21+02:00","startTime":1408288641993,"map":"turbine","mode":"instagib","server":"85.214.66.181:40000","players":{"IxcE":{"name":"IxcE","ip":"50.96.86.x","frags":0,"weapon":"rifle","fragLog":{"8":12,"4":6,"9":16,"5":9,"10":18,"6":10,"1":0,"2":3,"7":12,"3":4}},"Astoreth":{"name":"Astoreth","ip":"50.96.86.x","frags":0,"weapon":"rifle","fragLog":{"8":69,"4":33,"9":77,"5":38,"10":87,"6":49,"1":11,"2":19,"7":60,"3":26}}}}},
//                 |{"id":"2014-08-17T17:59:1802:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T17:59:1802:00::85.214.66.181:40000","_rev":"1-ae4d1c14b4b4c5293fad2720ca9d8246","simpleId":"2014-08-17T17:59:1802:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T17:59:18+02:00","startTime":1408291158993,"map":"turbine","mode":"efficiency","server":"85.214.66.181:40000","players":{"0321":{"name":"0321","ip":"74.91.101.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":21,"4":11,"9":24,"5":14,"10":24,"6":16,"1":2,"2":4,"7":17,"3":6}},"Astoreth":{"name":"Astoreth","ip":"50.96.86.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":37,"4":21,"9":41,"5":26,"10":42,"6":31,"1":3,"2":8,"7":32,"3":14}}}}},
//                 |{"id":"2014-08-17T18:28:2802:00::108.61.210.80:28785","key":null,"value":{"_id":"2014-08-17T18:28:2802:00::108.61.210.80:28785","_rev":"1-47b8bc1874505aa149ff2e2fa9b0adc6","simpleId":"2014-08-17T18:28:2802:00::108.61.210.80:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T18:28:28+02:00","startTime":1408292908109,"map":"turbine","mode":"efficiency","server":"108.61.210.80:28785","players":{"w00p|Art":{"name":"w00p|Art","ip":"146.255.147.x","frags":0,"weapon":"minigun","fragLog":{"8":72,"4":37,"9":81,"5":46,"10":89,"6":55,"1":10,"2":20,"7":62,"3":29}},".rC|Rexus":{"name":".rC|Rexus","ip":"86.166.100.x","frags":0,"weapon":"minigun","fragLog":{"8":54,"4":29,"9":61,"5":36,"10":67,"6":42,"1":8,"2":14,"7":47,"3":22}}}}},
//                 |{"id":"2014-08-17T18:34:0002:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T18:34:0002:00::85.214.66.181:40000","_rev":"1-dc9f86b0c2c67d93c35b3abb62ead559","simpleId":"2014-08-17T18:34:0002:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T18:34:00+02:00","startTime":1408293240991,"map":"turbine","mode":"efficiency","server":"85.214.66.181:40000","players":{"|noVI:Alli":{"name":"|noVI:Alli","ip":"91.63.21.x","frags":0,"weapon":"minigun","fragLog":{"8":63,"4":36,"9":71,"5":42,"10":78,"6":50,"1":9,"2":19,"7":58,"3":25}},"Astoreth":{"name":"Astoreth","ip":"50.96.86.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":44,"4":23,"9":51,"5":28,"10":56,"6":34,"1":6,"2":11,"7":41,"3":16}}}}},
//                 |{"id":"2014-08-17T18:40:1902:00::108.61.210.80:28785","key":null,"value":{"_id":"2014-08-17T18:40:1902:00::108.61.210.80:28785","_rev":"1-861fe2172e8fe63175effe123084d102","simpleId":"2014-08-17T18:40:1902:00::108.61.210.80:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T18:40:19+02:00","startTime":1408293619109,"map":"memento","mode":"efficiency","server":"108.61.210.80:28785","players":{"w00p|Art":{"name":"w00p|Art","ip":"146.255.147.x","frags":0,"weapon":"minigun","fragLog":{"8":57,"4":30,"9":62,"5":35,"10":68,"6":43,"1":7,"2":13,"7":50,"3":21}},".rC|Rexus":{"name":".rC|Rexus","ip":"86.166.100.x","frags":0,"weapon":"rifle","fragLog":{"8":37,"4":19,"9":43,"5":22,"10":48,"6":27,"1":3,"2":9,"7":32,"3":13}}}}},
//                 |{"id":"2014-08-17T18:45:1802:00::85.214.66.181:40000","key":null,"value":{"_id":"2014-08-17T18:45:1802:00::85.214.66.181:40000","_rev":"1-988e20faeb963946a9e2e6fb744122b6","simpleId":"2014-08-17T18:45:1802:00::85.214.66.181:40000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T18:45:18+02:00","startTime":1408293918994,"map":"duel7","mode":"instagib","server":"85.214.66.181:40000","players":{"|noVI:Alli":{"name":"|noVI:Alli","ip":"91.63.21.x","frags":64,"weapon":"rifle","fragLog":{"8":56,"4":26,"9":59,"5":34,"10":64,"6":40,"1":5,"2":13,"7":46,"3":20}},"Astoreth":{"name":"Astoreth","ip":"50.96.86.x","frags":64,"weapon":"rifle","fragLog":{"8":48,"4":20,"9":56,"5":26,"10":64,"6":31,"1":7,"2":9,"7":42,"3":14}}}}},
//                 |{"id":"2014-08-17T18:57:2502:00::108.61.210.80:28785","key":null,"value":{"_id":"2014-08-17T18:57:2502:00::108.61.210.80:28785","_rev":"1-37f9d3569444e86be9df0cc650dd3831","simpleId":"2014-08-17T18:57:2502:00::108.61.210.80:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T18:57:25+02:00","startTime":1408294645110,"map":"academy","mode":"efficiency","server":"108.61.210.80:28785","players":{"w00p|Art":{"name":"w00p|Art","ip":"146.255.147.x","frags":59,"weapon":"rocket launcher","fragLog":{"8":49,"4":28,"9":54,"5":34,"10":59,"6":38,"1":6,"2":13,"7":44,"3":21}},".rC|Rexus":{"name":".rC|Rexus","ip":"86.166.100.x","frags":46,"weapon":"rocket launcher","fragLog":{"8":36,"4":18,"9":40,"5":21,"10":46,"6":27,"1":3,"2":8,"7":32,"3":14}}},"winner":"w00p|Art"}},
//                 |{"id":"2014-08-17T19:28:1502:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T19:28:1502:00::85.214.66.181:20000","_rev":"1-af9d8e5b5b9746322c96dd60833dfc58","simpleId":"2014-08-17T19:28:1502:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T19:28:15+02:00","startTime":1408296495966,"map":"turbine","mode":"efficiency","server":"85.214.66.181:20000","players":{"wp.T4m!n0":{"name":"wp.T4m!n0","ip":"93.218.50.x","frags":71,"weapon":"minigun","fragLog":{"8":57,"4":26,"9":64,"5":32,"10":71,"6":40,"1":8,"2":14,"7":49,"3":21}},".rC|Medusah":{"name":".rC|Medusah","ip":"178.198.13.x","frags":77,"weapon":"minigun","fragLog":{"8":59,"4":28,"9":69,"5":35,"10":77,"6":42,"1":10,"2":17,"7":51,"3":22}}},"winner":".rC|Medusah"}},
//                 |{"id":"2014-08-17T19:38:2102:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T19:38:2102:00::85.214.66.181:20000","_rev":"1-181ff24d21282a252f98e12b9661412c","simpleId":"2014-08-17T19:38:2102:00::85.214.66.181:20000","duration":8,"playedAt":[5,1,6,2,7,3,8,4],"startTimeText":"2014-08-17T19:38:21+02:00","startTime":1408297101963,"map":"neonpanic","mode":"efficiency","server":"85.214.66.181:20000","players":{"wp.T4m!n0":{"name":"wp.T4m!n0","ip":"93.218.50.x","frags":2,"weapon":"rifle","fragLog":{"8":2,"4":-1,"5":0,"6":0,"1":1,"2":1,"7":1,"3":0}},".rC|Medusah":{"name":".rC|Medusah","ip":"178.198.13.x","frags":8,"weapon":"minigun","fragLog":{"8":8,"4":3,"5":4,"6":5,"1":0,"2":1,"7":7,"3":2}}},"winner":".rC|Medusah"}},
//                 |{"id":"2014-08-17T19:45:5402:00::85.214.66.181:20000","key":null,"value":{"_id":"2014-08-17T19:45:5402:00::85.214.66.181:20000","_rev":"1-d5905850dec7bfe5846df770f4df0d1b","simpleId":"2014-08-17T19:45:5402:00::85.214.66.181:20000","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T19:45:54+02:00","startTime":1408297554963,"map":"complex","mode":"instagib","server":"85.214.66.181:20000","players":{"wp.T4m!n0":{"name":"wp.T4m!n0","ip":"93.218.50.x","frags":73,"weapon":"rifle","fragLog":{"8":60,"4":28,"9":65,"5":38,"10":72,"6":42,"1":6,"2":13,"7":52,"3":18}},".rC|Medusah":{"name":".rC|Medusah","ip":"178.198.13.x","frags":123,"weapon":"rifle","fragLog":{"8":98,"4":46,"9":111,"5":62,"10":122,"6":78,"1":11,"2":24,"7":87,"3":37}}},"winner":"wp.T4m!n0"}},
//                 |{"id":"2014-08-17T20:15:4602:00::62.75.213.175:28785","key":null,"value":{"_id":"2014-08-17T20:15:4602:00::62.75.213.175:28785","_rev":"1-d3e0fb027e11bb058589cd81852509c9","simpleId":"2014-08-17T20:15:4602:00::62.75.213.175:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T20:15:46+02:00","startTime":1408299346130,"map":"turbine","mode":"instagib","server":"62.75.213.175:28785","players":{"|noVI:Luffy":{"name":"|noVI:Luffy","ip":"141.3.239.x","frags":0,"weapon":"rifle","fragLog":{"8":90,"4":46,"9":97,"5":55,"10":109,"6":69,"1":11,"2":22,"7":81,"3":35}},"wo0zy":{"name":"wo0zy","ip":"93.194.231.x","frags":0,"weapon":"rifle","fragLog":{"8":78,"4":38,"9":91,"5":48,"10":98,"6":58,"1":6,"2":18,"7":66,"3":31}}}}},
//                 |{"id":"2014-08-17T20:26:3702:00::62.75.213.175:28785","key":null,"value":{"_id":"2014-08-17T20:26:3702:00::62.75.213.175:28785","_rev":"1-657820f85450b06235e4fcaac076e4dc","simpleId":"2014-08-17T20:26:3702:00::62.75.213.175:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T20:26:37+02:00","startTime":1408299997130,"map":"complex","mode":"instagib","server":"62.75.213.175:28785","players":{"|noVI:Luffy":{"name":"|noVI:Luffy","ip":"141.3.239.x","frags":0,"weapon":"rifle","fragLog":{"8":92,"4":51,"9":104,"5":61,"10":112,"6":71,"1":12,"2":25,"7":82,"3":38}},"wo0zy":{"name":"wo0zy","ip":"93.194.231.x","frags":0,"weapon":"rifle","fragLog":{"8":79,"4":44,"9":90,"5":50,"10":96,"6":60,"1":15,"2":25,"7":68,"3":34}}}}},
//                 |{"id":"2014-08-17T20:37:3402:00::62.75.213.175:28785","key":null,"value":{"_id":"2014-08-17T20:37:3402:00::62.75.213.175:28785","_rev":"1-f1abccd35103dc6db3f828c591fc548c","simpleId":"2014-08-17T20:37:3402:00::62.75.213.175:28785","duration":10,"playedAt":[5,10,1,6,9,2,7,3,8,4],"startTimeText":"2014-08-17T20:37:34+02:00","startTime":1408300654131,"map":"turbine","mode":"efficiency","server":"62.75.213.175:28785","players":{"|noVI:Luffy":{"name":"|noVI:Luffy","ip":"141.3.239.x","frags":0,"weapon":"rocket launcher","fragLog":{"8":79,"4":38,"9":89,"5":48,"10":99,"6":57,"1":10,"2":18,"7":70,"3":29}},"wo0zy":{"name":"wo0zy","ip":"93.194.231.x","frags":0,"weapon":"minigun","fragLog":{"8":53,"4":23,"9":64,"5":30,"10":71,"6":38,"1":6,"2":11,"7":45,"3":17}}}}}
//                 |]}""".stripMargin
//
//}