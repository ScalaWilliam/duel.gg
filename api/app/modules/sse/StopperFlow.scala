package modules.sse

import akka.stream.FanInShape.{Init, Name}
import akka.stream.scaladsl.{FlexiMerge, Flow, Source}
import akka.stream.{Attributes, FanInShape}

import scala.concurrent.Promise

/** Courtesy of http://stackoverflow.com/a/31425051/2789308 **/
object StopperFlow {

  def apply[In](): Flow[In, In, Promise[Unit]] = {
    val stopperSource = Source.lazyEmpty[Unit]
    import akka.stream.scaladsl._
    import FlowGraph.Implicits._
    Flow(stopperSource) { implicit builder =>
      stopper =>
        val stopperMerge = builder.add(new StopperMerge[In]())

        stopper ~> stopperMerge.stopper

        (stopperMerge.in, stopperMerge.out)
    }

  }

  private class StopperMergeShape[A](_init: Init[A] = Name("StopperFlow")) extends FanInShape[A](_init) {
    val in = newInlet[A]("in")
    val stopper = newInlet[Unit]("stopper")

    override protected def construct(init: Init[A]): FanInShape[A] = new StopperMergeShape[A](init)
  }

  private class StopperMerge[In] extends FlexiMerge[In, StopperMergeShape[In]](
    new StopperMergeShape(), Attributes.name("StopperMerge")) {

    import FlexiMerge._

    override def createMergeLogic(p: PortT) = new MergeLogic[In] {
      override def initialState =
        State[In](Read(p.in)) { (ctx, input, element) =>
          ctx.emit(element)
          SameState
        }

      override def initialCompletionHandling = eagerClose
    }
  }
}
