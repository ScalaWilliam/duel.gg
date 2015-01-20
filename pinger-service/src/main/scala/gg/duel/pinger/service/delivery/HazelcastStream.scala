//package gg.duel.pinger.service.delivery
//
//import akka.actor.{ActorSystem, Props}
//import akka.actor.ActorDSL._
//import akka.stream.{FlowMaterializer, MaterializerSettings}
//import akka.stream.actor.ActorPublisher
//import akka.stream.actor.ActorPublisherMessage.Cancel
//import akka.stream.actor.ActorPublisherMessage.Request
//import akka.stream.scaladsl.Flow
//import com.hazelcast.client.HazelcastClient
//import com.hazelcast.core._
//import com.hazelcast.instance.HazelcastInstanceImpl
//
//import scala.concurrent.ExecutionContext
//
//object SimpleTest extends App {
//  implicit val sissy = ActorSystem("aws")
//
//  val publisher = ActorPublisher.apply(actor(new Act with ActorPublisher[String] {
//    become {
//      case Request(n) => (1 to n).foreach(_ => onNext("Hello!"))
//      case Cancel => context stop self
//    }
//  }))
//  implicit val settings = FlowMaterializer( MaterializerSettings())
//  Flow.apply(publisher).foreach(println)
//}
//
//object HCStreamApp extends App {
//  implicit val sissy = ActorSystem("aws")
//
//  val hs = Hazelcast.newHazelcastInstance()
//  val wutHs = hs.getTopic[String]("wut")
//
//  import ExecutionContext.Implicits.global
//  import concurrent.duration._
//  sissy.scheduler.schedule(2.seconds, 2.seconds)(wutHs.publish("cool!"))
//
//  val wutHc = HazelcastClient.newHazelcastClient().getTopic[String]("wut")
//  val publisher = ActorPublisher.apply(sissy.actorOf(HazelcastTopicStream.props[String](wutHc)))
//  implicit val settings = FlowMaterializer( MaterializerSettings())
//}
//
//
