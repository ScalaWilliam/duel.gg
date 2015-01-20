package us.woop.pinger.bobby

import akka.actor.ActorSystem
import com.hazelcast.client.HazelcastClient
import com.hazelcast.core.{Message, MessageListener}
import org.pircbotx.hooks.events.{InviteEvent, ConnectEvent}
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.{Configuration, PircBotX}
import spray.http.{HttpRequest, HttpResponse}
import spray.client.pipelining._
import scala.concurrent.Future

object BobbyApp extends App {
  System.setProperty("hazelcast.logging.type","slf4j")
  implicit val as = ActorSystem("Goodies")
  val mainChannel = System.getProperty("bobby.channel", "#duel.gg")
  import as.dispatcher
  val restUrl = System.getProperty("pinger.basex.context", "http://127.0.0.1:8984/rest/db-stage")
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  HazelcastClient.newHazelcastClient().getTopic[String]("new-duels").addMessageListener(new MessageListener[String] {
    override def onMessage(message: Message[String]): Unit = {
      val duelId = message.getMessageObject
      for {
        response <- QueryDuel.apply(pipeline)(restUrl)(duelId)
      } for {
        line <- response
      } {
        bot.sendIRC().message(mainChannel, line)
      }
    }
  })
  val config = new Configuration.Builder()
    .setName("DuelGG")
    .setServerHostname("Burstfire.UK.EU.GameSurge.net")
    .setLogin("WoopClan")
//    .setIdentServerEnabled(true)
    .setVersion("DuelGG IRC bot :: http://duel.gg/")
    .setRealName("DuelGG IRC bot :: http://duel.gg/")
    .addListener(new ListenerAdapter[PircBotX] {
      override def onConnect(e: ConnectEvent[PircBotX]) = {
        e.getBot.sendIRC().message("AuthServ@Services.GameSurge.net", "auth DuelGG 14nTLcaj")
        import concurrent.duration._
        as.scheduler.scheduleOnce(10.seconds){
          e.getBot.sendIRC().joinChannel(mainChannel)
        }
      }
      override def onInvite(e: InviteEvent[PircBotX]) = {
        e.getBot.sendIRC().joinChannel(e.getChannel)
      }
    })
    .setAutoReconnect(true)
    .buildConfiguration()

  val bot = new PircBotX(config)

  bot.startBot()

}