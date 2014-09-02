//package us.woop.pinger.service.delivery
//import akka.actor.ActorDSL._
//import akka.stream.actor.ActorPublisher
//import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
//
//trait EventDrivenStream[T] extends Act with ActorPublisher[T] {
//
//  become(empty)
//
//  case class ReceivedMessage(value: T)
//
//  def emptyAndWaiting(currentN: Int): Receive = {
//    case ReceivedMessage(message) =>
//      onNext(message)
//      become(emptyAndWaiting(currentN - 1))
//    case Request(n) =>
//      become(emptyAndWaiting(currentN + n))
//    case Cancel =>
//      context stop self
//    case other => println(other)
//  }
//
//  def empty: Receive = {
//    case ReceivedMessage(message) =>
//      become(nonEmpty(Vector(message)))
//    case Request(n) =>
//      become(emptyAndWaiting(n))
//    case Cancel =>
//      context stop self
//    case other => println(other)
//  }
//
//  def nonEmpty(data: Vector[T]): Receive = {
//    case ReceivedMessage(message) =>
//      become(nonEmpty(data :+ message))
//    case Request(n) =>
//
//      val (send, keep) = data.splitAt(n)
//      val notFulfilled = n - send.size
//
//      if ( notFulfilled > 0 ) {
//        become(emptyAndWaiting(notFulfilled))
//      } else if (keep.isEmpty) {
//        become(empty)
//      } else {
//        become(nonEmpty(keep))
//      }
//
//      send foreach this.onNext
//    case Cancel =>
//      context stop self
//    case other => println(other)
//  }
//
//}
