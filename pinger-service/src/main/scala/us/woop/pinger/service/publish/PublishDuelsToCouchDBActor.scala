package us.woop.pinger.service.publish

import akka.actor.ActorSystem
import us.woop.pinger.analytics.processing.DuelMaker.CompletedDuel

import scala.concurrent.{Await, Future}

class PublishDuelsToCouchDBActor {

}
object PublishDuelsToCouchDBActor extends App {
  lazy val result = {import spray.client.pipelining._
  import spray.http._
    implicit val ac = ActorSystem("whut")
    import ac.dispatcher
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
//    val response =
//      pipeline(Put("http://127.0.0.1:5984/cool/such_wow", """{"this": "cool"}"""))

    val response = for {
      r <- pipeline(Get("http://127.0.0.1:5984/cool/such_wow"))
      i <- pipeline(Put("http://127.0.0.1:5984/cool/such_wow", r.entity))
    } yield i
    import scala.concurrent.duration._
    val out = Await.result(response, 5.seconds)
    println(out)
  }
}
