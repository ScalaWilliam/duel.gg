package actors

import akka.actor.Props
import akka.stream.actor.ActorPublisher

import scala.annotation.tailrec
import scala.reflect.ClassTag


object ForwardMessages {
  def props[T](implicit ct: ClassTag[T]): Props = Props(new ForwardMessages[T])
}

class ForwardMessages[T](implicit classTag: ClassTag[T]) extends ActorPublisher[T] {

  import akka.stream.actor.ActorPublisherMessage._

  val MaxBufferSize = 100
  var buf = Vector.empty[T]

  println(s"Subscribing self to $classTag")
  context.system.eventStream.subscribe(self, classTag.runtimeClass)

  def receive = {
    case classTag(item) =>
      if (buf.isEmpty && totalDemand > 0)
        onNext(item)
      else {
        buf :+= item
        deliverBuf()
      }
    case Request(_) =>
      deliverBuf()
    case Cancel =>
      context.stop(self)
  }

  @tailrec final def deliverBuf(): Unit =
    if (totalDemand > 0) {
      /*
       * totalDemand is a Long and could be larger than
       * what buf.splitAt can accept
       */
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use foreach onNext
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use foreach onNext
        deliverBuf()
      }
    }
}

