package us.woop.pinger.client


/**
 * Class to perform simple transformations on stuff and push them back into the eventStream.
 */
//class EventBusProcessor[T](input: PartialFunction[T, Seq[AnyRef]])(implicit e: ) extends Act with ActorLogging {
//
//  whenStarting {
//    context.system.eventStream.subscribe(self, classOf[T])
//  }
//
//  private val selectT: PartialFunction[Any, T] = { case m: T => m }
//
//  become {
//    selectT andThen input andThen (_ foreach context.system.eventStream.publish)
//  }
//
//}
//