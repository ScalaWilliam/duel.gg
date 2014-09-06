package plugins

import akka.actor.{Props, ActorRef}
import com.hazelcast.core.{Hazelcast, Message, MessageListener}
import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.json.{JsValue, JsString, JsObject}
import plugins.DuelsStream.NewDuelContent
class DuelsStream(implicit app: Application) extends Plugin {

  override def enabled = true
  lazy val hazelcast = {
    Hazelcast.newHazelcastInstance()
  }
  lazy val newDuelsTopic = hazelcast.getTopic[String]("new-duels")
  lazy val messageListener = new MessageListener[String] {
    override def onMessage(p1: Message[String]): Unit = {
      import scala.concurrent.ExecutionContext.Implicits.global
      val newId = p1.getMessageObject
      for {
        xmlO <- DuelsInterface.duelsInterface.getIndexDuel(newId)
      } for {
        xml <- xmlO
        newObject = JsObject(Seq(
          "new duel id" -> JsString(newId),
          "new index item" -> JsString(s"$xml")
        ))
      } {
        val message = NewDuelContent(newObject)
        Akka.system.eventStream.publish(message)
      }
    }
  }

  var listenerId: String = _

  override def onStart(): Unit = {
    listenerId = newDuelsTopic.addMessageListener(messageListener)
  }

  override def onStop(): Unit = {
    if ( listenerId != null ) {
      newDuelsTopic.removeMessageListener(listenerId)
    }
    hazelcast.shutdown()
  }

  def createListenerActor(out: ActorRef) = {
    import akka.actor.ActorDSL._
    Props(new Act {
      whenStarting {
        context.system.eventStream.subscribe(self, classOf[NewDuelContent])
      }
      become {
        case NewDuelContent(content) => out ! content
      }
    })

  }

}
object DuelsStream {

  case class NewDuel(webId: String)
  case class NewDuelContent(content: JsValue)
  def duelsStream: DuelsStream = Play.current.plugin[DuelsStream]
    .getOrElse(throw new RuntimeException("DuelsStream plugin not loaded"))
}

