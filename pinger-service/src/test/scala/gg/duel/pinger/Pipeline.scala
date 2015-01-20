package gg.duel.pinger

import akka.actor.ActorSystem

object Pipeline {
  def pipeline(implicit as: ActorSystem) = {
    import spray.http._
    import spray.httpx.encoding.{Gzip, Deflate}
    import spray.httpx.SprayJsonSupport._
    import spray.client.pipelining._
    import as.dispatcher
    sendReceive
  }
}