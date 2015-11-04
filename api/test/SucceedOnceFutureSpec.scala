import akka.actor.ActorSystem
import akka.agent.Agent
import lib.SucceedOnceFuture
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * Created on 04/11/2015.
 */
class SucceedOnceFutureSpec
  extends WordSpec
  with ScalaFutures
  with Matchers
  with BeforeAndAfterAll {

  import ExecutionContext.Implicits.global
  import concurrent.duration._

  override protected def afterAll(): Unit = {
    super.afterAll()
    Await.ready(actorSystem.terminate(), 1.second)
  }

  implicit val actorSystem = ActorSystem()
  implicit val scheduler = actorSystem.scheduler

  "The succeeder" must {
    "Succeed immediately" in {
      def future = Future.successful(1)
      val succ = new SucceedOnceFuture(future)(_ => 0.seconds)
      succ.value.futureValue shouldBe 1
      succ.finalValue.futureValue shouldBe 1
    }
    "Fail first, succeed third" in {
      val decremantAgent = Agent(5)

      def future = decremantAgent.alter(_ - 1) map {
        case 0 => "great"
        case n => throw new RuntimeException(s"It fails = $n")
      }
      val succ = new SucceedOnceFuture(future)(_ => 5.millis)
      a[RuntimeException] shouldBe thrownBy {
        succ.value.futureValue
      }
      succ.finalValue.futureValue shouldBe "great"
    }
  }
}
