package us.woop.pinger.service.delivery

import akka.actor.Props
import com.hazelcast.core.{MessageListener, Message, ITopic}

object HazelcastTopicStream {
  def props[T](stream: ITopic[T]) = Props(new HazelcastTopicStream[T](stream))
}

class HazelcastTopicStream[T](topic: ITopic[T]) extends EventDrivenStream[Message[T]] {

  val listenerRegistrationId = topic.addMessageListener(new MessageListener[T] {
    override def onMessage(message: Message[T]): Unit = {
      self ! ReceivedMessage(message)
    }
  })

  whenStopping {
    topic.removeMessageListener(listenerRegistrationId)
  }

}
