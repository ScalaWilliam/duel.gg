package us.woop.pinger.persistence

import java.util.concurrent.CountDownLatch
import scala.annotation.tailrec
import scala.util.control.NonFatal


object Memoize {
  def apply[A, B](f: A => B): A => B =
    new Function1[A, B] {
      private[this] var memo = Map.empty[A, Either[CountDownLatch, B]]
      @tailrec private[this] def missing(a: A): B =
        synchronized {
          memo.get(a) match {
            case None =>
              val latch = new CountDownLatch(1)
              memo = memo + (a -> Left(latch))
              Left(latch)

            case Some(other) =>
              Right(other)
          }
        } match {
          case Right(Right(b)) =>
            b
          case Right(Left(latch)) =>
            latch.await()
            missing(a)
          case Left(latch) =>
            val b =
              try {
                f(a)
              } catch {
                case NonFatal(t) =>
                  synchronized { memo = memo - a }
                  latch.countDown()
                  throw t
              }
            synchronized { memo = memo + (a -> Right(b)) }
            latch.countDown()
            b
        }

      override def apply(a: A): B =
        memo.get(a) match {
          case Some(Right(b)) => b
          case _ => missing(a)
        }
    }
}