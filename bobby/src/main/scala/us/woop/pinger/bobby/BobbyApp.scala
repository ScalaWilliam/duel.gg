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
  import as.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  HazelcastClient.newHazelcastClient().getTopic[String]("new-duels").addMessageListener(new MessageListener[String] {
    override def onMessage(message: Message[String]): Unit = {
      val duelId = message.getMessageObject
      for {
        response <- QueryDuel.apply(pipeline)("""http://prod-b.duel.gg:8984/rest/db-stage""")(duelId)
      } for {
        line <- response
      } {
        bot.sendIRC().message("#wat", line)
        bot.sendIRC().message("#duel.gg", line)
      }
    }
  })

  val config = new Configuration.Builder()
    .setName("DuelGG")
    .setServerHostname("Burstfire.UK.EU.GameSurge.net")
    .addAutoJoinChannel("#wat")
    .setLogin("WoopClan")
//    .setIdentServerEnabled(true)
    .setVersion("DuelGG IRC bot :: http://duel.gg/")
    .setRealName("DuelGG IRC bot :: http://duel.gg/")
    .addListener(new ListenerAdapter[PircBotX] {
      override def onConnect(e: ConnectEvent[PircBotX]) = {
        e.getBot.sendIRC().message("AuthServ@Services.GameSurge.net", "auth DuelGG 14nTLcaj")
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