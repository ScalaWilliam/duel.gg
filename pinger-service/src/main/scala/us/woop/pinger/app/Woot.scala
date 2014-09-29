package us.woop.pinger.app

import java.io.{FileOutputStream, File}
import akka.actor.ActorDSL._
import akka.actor._
import akka.routing.RoundRobinPool
import com.hazelcast.core.{ItemEvent, ItemListener, HazelcastInstance}
import us.WSAsyncDuelPersister
import us.woop.pinger.analytics.better.BetterMultiplexedReader.{SInitial, SFoundGame, SIteratorState}
import us.woop.pinger.analytics.worse.DuelMaker.SimpleCompletedDuel
import us.woop.pinger.app.Woot.{NewlyAddedDuel, MetaCompletedDuel, JournalGenerator, RotateMeta}
import us.woop.pinger.data.Server
import us.woop.pinger.data.journal.{SauerBytesWriter, IterationMetaData}
import us.woop.pinger.service.PingPongProcessor.ReceivedBytes
import us.woop.pinger.service.{BaseXServers, PingerController}
import us.woop.pinger.service.PingerController.{Unmonitor, Monitor}
import us.woop.pinger.service.analytics.JournalBytes

import scala.concurrent.Future

class Woot(hazelcast: HazelcastInstance, persister: WSAsyncDuelPersister, journalGenerator: JournalGenerator, disableHashing: Boolean) extends Act {
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

  val basexServers = context.actorOf(BaseXServers.props(persister))

  val pingerController =
    context.actorOf(RoundRobinPool(1, supervisorStrategy = OneForOneStrategy(){
      case _: ActorInitializationException => SupervisorStrategy.Restart
      case _: DeathPactException  => Stop
      case _: ActorKilledException => SupervisorStrategy.Restart
      case _: Exception => SupervisorStrategy.Resume
    }).props(PingerController.props(disableHashing = disableHashing, context.self)),
      "pingerController")

  val metaContext =
  actor(context)(new Act {

    var journalBytes: ActorRef = _
    def metas(meta: IterationMetaData, state: SIteratorState): Unit = {

      hazelcast.getTopic[String]("meta").publish(meta.toJson)

//      JournalSauerBytes.props(meta)

      if ( journalBytes != null ) {
        journalBytes ! Stop
      }

      import scala.concurrent.ExecutionContext.Implicits.global

      persister.pushMeta(meta)

      journalBytes = context.actorOf(JournalBytes.props(meta, journalGenerator.generate(meta)))

      become(metad(meta, state))

      context.parent ! meta
    }

    def metad(meta: IterationMetaData, state: SIteratorState): Receive = {
      case RotateMeta =>
        metas(IterationMetaData.build, state)
      case m: ReceivedBytes if journalBytes != null =>
        journalBytes ! m.toSauerBytes
        val nextState = state.next(m.toSauerBytes)
        nextState match {
          case SFoundGame(_, game) =>
            self ! MetaCompletedDuel(meta, game.copy(metaId = Option(meta.id)))
          case _ =>
        }
        become(metad(meta, nextState))
      case g: MetaCompletedDuel if sender() == self =>
        context.parent ! g
      case g: JournalBytes.WritingStopped =>
        context.parent ! g
    }

    whenStarting {
      metas(IterationMetaData.build, SInitial)
    }

  })

  val completedDuelPublish =
  actor(context)(new Act {
    whenStarting {
      import scala.concurrent.ExecutionContext.Implicits.global
      persister.createDatabase
    }

    become {
      case nd: NewlyAddedDuel =>
        for { att <- nd.duel \ "@web-id" } {
          hazelcast.getTopic[String]("new-duels").publish(att.text)
          context.parent ! nd
        }
      case d: MetaCompletedDuel =>
        val sd = d.completedDuel.copy(metaId = Option(d.metaId.id))

        import scala.concurrent.ExecutionContext.Implicits.global
        // left => existing ID
        // right => new ID
        val pushDuelResult: Future[Either[scala.xml.Elem, Option[scala.xml.Elem]]] = for {
          existingDuel <- persister.getSimilarDuel(sd)
          result <- existingDuel match {
            case Some(existingId) => Future(Left(existingId))
            case None => for {
              _ <- persister.pushDuel(sd)
              haveIt <- persister.getSimilarDuel(sd)
            } yield Right(haveIt)
          }
        } yield result

        for {
          Right(Some(newDuel)) <- pushDuelResult
        } {
          self ! NewlyAddedDuel(newDuel)
        }
    }
  })

  become {
    case m: Monitor =>
      pingerController ! m
    case m: Unmonitor =>
      pingerController ! m
    case r: ReceivedBytes =>
      metaContext ! r
      context.parent ! r
    case g: MetaCompletedDuel =>
      completedDuelPublish ! g
      context.parent ! g
    case m: IterationMetaData =>
      context.parent ! m
    case RotateMeta =>
      metaContext ! RotateMeta
    case nd: NewlyAddedDuel =>
      context.parent ! nd
    case 1 => println("yay")
  }

}
object Woot {

  def props(hazelcast: HazelcastInstance,
             persister: WSAsyncDuelPersister, journalGenerator: JournalGenerator, disableHashing: Boolean) = {
    Props(classOf[Woot], hazelcast, persister, journalGenerator, disableHashing)
  }

  case class MetaCompletedDuel(metaId: IterationMetaData, completedDuel: SimpleCompletedDuel)

  case object RotateMeta

  case class JournalGenerator
  (
  generate: IterationMetaData => JournalBytes.Writer
    )
case class NewlyAddedDuel(duel: scala.xml.Elem)
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