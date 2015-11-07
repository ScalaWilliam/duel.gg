package lib

import akka.actor.Scheduler
import akka.agent.Agent

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.util.{Success, Failure}

/**
 * Return a current future's success / failure
 * But retry if that has failed, until it succeeds.
  */
class SucceedOnceFuture[T](f: => Future[T])
                          (delay: Int => FiniteDuration)
                          (implicit executionContext: ExecutionContext,
                           scheduler: Scheduler) {

  private val successPromise = Promise[T]()

  private val initialRun = f

  private val state: Agent[State] = Agent(RunningFirst(initialRun))

  initialRun.onComplete {
    case Failure(_) =>
      state.send(Retrying(
        value = akka.pattern.after(duration = delay(1), using = scheduler)(f),
        failed = value,
        retryNo = 1
      ))
    case Success(_) => state.send(Completed(value = value, tries = 0))
  }

  private sealed trait State {
    def value: Future[T]
  }

  private case class RunningFirst(value: Future[T]) extends State

  private case class Retrying(value: Future[T], failed: Future[T], retryNo: Int) extends State {
    value.onComplete {
      case Failure(_) =>
        state.send(Retrying(
          value = akka.pattern.after(duration = delay(retryNo + 1), using = scheduler)(f),
          failed = value,
          retryNo = retryNo + 1
        ))
      case Success(_) =>
        state.send(Completed(value = value, tries = retryNo))
    }
  }

  private case class Completed(value: Future[T], tries: Int) extends State {
    successPromise.completeWith(value)
  }

  def value: Future[T] = state.get().value

  def finalValue: Future[T] = successPromise.future

}

