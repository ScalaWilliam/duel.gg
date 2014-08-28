package us.woop.pinger.app

import java.io.{FileOutputStream, File}

import akka.actor.ActorDSL._
import akka.actor._
import akka.routing.RoundRobinPool
import com.hazelcast.core.{ItemEvent, ItemListener, HazelcastInstance}
import us.WSAsyncDuelPersister
import us.woop.pinger.app.Woot.{JournalGenerator, RotateMeta}
import us.woop.pinger.data.Server
import us.woop.pinger.data.journal.{SauerBytesWriter, IterationMetaData}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.PingerController
import us.woop.pinger.service.PingerController.{Unmonitor, Monitor}
import us.woop.pinger.service.analytics.JournalBytes
import us.woop.pinger.service.delivery.JournalSauerBytes
import us.woop.pinger.service.delivery.JournalSauerBytes.WritingStopped

class Woot(hazelcast: HazelcastInstance, persister: WSAsyncDuelPersister, journalGenerator: JournalGenerator) extends Act {
  val serversSet = hazelcast.getSet[String]("servers")
  whenStarting {
    import collection.JavaConverters._
    serversSet.asScala.foreach(s => self ! Monitor(Server(s)))
    serversSet.addItemListener(new ItemListener[String]() {
      override def itemAdded(item: ItemEvent[String]): Unit = {
        self ! Monitor(Server(item.getItem))
      }
      override def itemRemoved(item: ItemEvent[String]): Unit = {
        self ! Unmonitor(Server(item.getItem))
      }
    }, true)
  }

  val pingerController =
    context.actorOf(RoundRobinPool(1, supervisorStrategy = OneForOneStrategy(){
      case _: ActorInitializationException => SupervisorStrategy.Restart
      case _: DeathPactException  => Stop
      case _: ActorKilledException => SupervisorStrategy.Restart
      case _: Exception => SupervisorStrategy.Resume
    }).props(PingerController.props(context.self)),
      "pingerController")

  val metaContext =
  actor(context)(new Act {

    var journalBytes: ActorRef = _
    def metas(meta: IterationMetaData): Unit = {
      hazelcast.getTopic[String]("meta").publish(meta.toJson)

//      JournalSauerBytes.props(meta)

      if ( journalBytes != null ) {
        journalBytes ! Stop
      }
      journalBytes = context.actorOf(JournalBytes.props(meta, journalGenerator.generate(meta)))

      become(metad(meta))

      context.parent ! meta
    }




    def metad(meta: IterationMetaData): Receive = {
      case RotateMeta =>
        metas(IterationMetaData.build)
      case m: ReceivedBytes if journalBytes != null =>
        journalBytes ! m.toSauerBytes
      case g: JournalBytes.WritingStopped =>
        context.parent ! g
    }
    whenStarting {
      metas(IterationMetaData.build)
    }
  })

  become {
    case m: Monitor => pingerController ! m
    case m: Unmonitor => pingerController ! m
    case r: ReceivedBytes => context.parent ! r
    case m: IterationMetaData => context.parent ! m
    case RotateMeta => metaContext ! RotateMeta
    case 1 => println("yay")
  }

}
object Woot {
  def props(hazelcast: HazelcastInstance,
             persister: WSAsyncDuelPersister, journalGenerator: JournalGenerator) = {
    Props(classOf[Woot], hazelcast, persister, journalGenerator)
  }
  case object RotateMeta

  case class JournalGenerator
  (
  generate: IterationMetaData => JournalBytes.Writer
    )

  object JournalGenerator {
    def standard = JournalGenerator(
      imd => {
        val filename = new File(s"""sb-${imd.id}.log""")
        val fileStream = new FileOutputStream(filename)
        JournalBytes.Writer(
          SauerBytesWriter.createInjectedWriter(b => {
          fileStream.write(b)
            fileStream.flush()
        }),
          () => { fileStream.flush(); fileStream.close() }
        )

      }
    )
  }
}