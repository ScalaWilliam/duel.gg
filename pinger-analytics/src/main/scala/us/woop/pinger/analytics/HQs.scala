package us.woop.pinger.analytics

import com.hazelcast.core.{ItemEvent, ItemListener, Hazelcast}
import scala.concurrent.{Await, Future}

object HQs extends App {

  /***
    * Persist the following things:
    * Sauer bytes -> Filesystem
    * Bytes metadata -> Filesystem & BaseX
    *
    *
    */
  // things we want to store:
  /** *
    *
    * * Metadata
    */
  // push to queue: recorded MetaData IDs with metadata values

  // metadata

  //  def woopQueues =
  import scala.concurrent.ExecutionContext.Implicits.global
  def haz = Future { Hazelcast.newHazelcastInstance() }
  import concurrent.duration._
  val hc :: hc2 :: hc3 :: Nil = Await.result(Future.sequence(List.fill(3)(haz)), 20.seconds)

  val q = hc.getQueue[Int]("YAY")
  val q2 = hc2.getQueue[Int]("YAY")
  val q3 = hc3.getQueue[Int]("YAY")

  q.addItemListener(new ItemListener[Int] {
    override def itemAdded(item: ItemEvent[Int]): Unit = {
      println("Q - added", item)
      println("Q - wait", q.poll())
      q.peek()
    }
    override def itemRemoved(item: ItemEvent[Int]): Unit = {
      println("Q - removed", item)
    }
  }, true)

  q2.addItemListener(new ItemListener[Int] {
    override def itemAdded(item: ItemEvent[Int]): Unit = {
      println("Q2 - added", item)
      println("Q2 - wait", q.poll())
      q.peek()
    }
    override def itemRemoved(item: ItemEvent[Int]): Unit = {
      println("Q2 - removed", item)
    }
  }, true)

  q3.offer(123)

}
