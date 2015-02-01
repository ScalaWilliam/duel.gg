package gg.duel.pinger.app

import java.io.{FileOutputStream, File}
import akka.actor.ActorDSL._
import akka.actor._
import akka.routing.RoundRobinPool
import com.hazelcast.core.{ItemEvent, ItemListener, HazelcastInstance}
import gg.duel.pinger.analytics.MultiplexedReader
import gg.duel.pinger.analytics.ctf.data.SimpleCompletedCTF
import gg.duel.pinger.analytics.duel.{LiveDuel, TransitionalBetterDuel, SimpleCompletedDuel}
import gg.duel.pinger.analytics.duel.StreamedSimpleDuelMaker.ZInDuelState
import gg.duel.pinger.service.DemoLoader.DemoDownloaded
import gg.duel.pinger.service.{DemoLoader, WSAsyncGamePersister, BaseXServers, PingerController}
import gg.duel.pinger.analytics.MultiplexedReader._
import gg.duel.pinger.app.Woot._
import gg.duel.pinger.data.Server
import gg.duel.pinger.data.journal.{SauerBytesWriter, IterationMetaData}
import gg.duel.pinger.service.PingPongProcessor.ReceivedBytes
import gg.duel.pinger.service.PingerController.{Unmonitor, Monitor}
import gg.duel.pinger.service.analytics.JournalBytes

import scala.concurrent.Future

class Woot(hazelcast: HazelcastInstance, persister: WSAsyncGamePersister, journalGenerator: JournalGenerator, disableHashing: Boolean, saveDemosToO: Option[File]) extends Act {
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

  superviseWith {
    import concurrent.duration._
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Throwable => Stop
    }
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

  val pushLiveUpdates =
    actor(context)(new Act with ActorLogging {
      case object CleanupOld
      val hazelcastStatusMap = hazelcast.getMap[String, String]("live-duel-updates")
      whenStarting {
        context.system.eventStream.subscribe(self, classOf[LiveDuelUpdated])
        import context.dispatcher
        import concurrent.duration._
        context.system.scheduler.schedule(5.seconds, 30.seconds, self, CleanupOld)
        hazelcastStatusMap.clear()
      }
      val currentDetail = scala.collection.mutable.Map.empty[Server, Option[LiveDuel]]
      become {
        case LiveDuelUpdated(server, liveDuelO) =>
          currentDetail += server -> liveDuelO
          liveDuelO match {
            case Some(liveDuel) => hazelcastStatusMap.put(server.toString, liveDuel.toXml.toString)
            case None => hazelcastStatusMap.remove(server.toString)
          }
        // remove servers that may have disappeared whilst in the middle of a duel
        case CleanupOld =>
          val removeServers = for {
            (server, Some(liveDuel)) <- currentDetail.toList
            // 20 minutes
            if System.currentTimeMillis - liveDuel.startTime > (20 * 60 * 1000)
          } yield server
          removeServers foreach currentDetail.remove
          removeServers map (_.toString) foreach hazelcastStatusMap.remove
      }
    })

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
          case SFoundGame(_, CompletedGame(Left(duel), _)) =>
            self ! MetaCompletedDuel(meta, duel.copy(metaId = Option(meta.id)))
          case SFoundGame(_, CompletedGame(Right(ctf), _)) =>
            self ! MetaCompletedCtf(meta, ctf.copy(metaId = Option(meta.id)))
          case _ =>
        }
        val liveDuelO = for {
          SProcessing(MProcessing(serverStates, _)) <- Option(nextState)
          ZInDuelState(_, td: TransitionalBetterDuel) <- serverStates.get(m.server)
          liveDuel <- td.liveDuel.toOption
        } yield liveDuel
        pushLiveUpdates ! LiveDuelUpdated(m.server, liveDuelO)
        become(metad(meta, nextState))
      case g: MetaCompletedDuel if sender() == self =>
        context.parent ! g
      case g: MetaCompletedCtf if sender() == self =>
        context.parent ! g
      case g: JournalBytes.WritingStopped =>
        context.parent ! g
    }

    whenStarting {
      metas(IterationMetaData.build, SInitial)
    }

  })

  val demoLoader = {
    for { saveDemosTo <- saveDemosToO }
      yield {
      saveDemosTo.mkdirs()
      context.actorOf(DemoLoader.props(saveDemosTo, persister))
    }
  }

  val completedDuelPublish =
  actor(context)(new Act with ActorLogging {
    whenStarting {
      import scala.concurrent.ExecutionContext.Implicits.global
      persister.createDatabase
    }

    become {
      case nd: NewlyAddedDuel =>
        for { att <- nd.duel \ "@int-id" } {
          hazelcast.getTopic[String]("new-duels").publish(att.text)
          context.parent ! nd
        }
      case nc: NewlyAddedCtf =>
        for { att <- nc.ctf \ "@int-id" } {
          hazelcast.getTopic[String]("new-ctfs").publish(att.text)
          context.parent ! nc
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
            case None =>
              // try to push three times
              val tryToPush = persister.pushDuel(sd) recoverWith {
                case e => persister.pushDuel(sd)
              } recoverWith {
                case e => persister.pushDuel(sd)
              }
              for {
                _ <- tryToPush
                haveIt <- persister.getSimilarDuel(sd)
              } yield Right(haveIt)
          }
        } yield result
        pushDuelResult onFailure {
          case f => log.error(f, "Failed to publish duel result")
        }
        for {
          Right(Some(newDuel)) <- pushDuelResult
        } {
          self ! NewlyAddedDuel(newDuel)
        }
      case d: MetaCompletedCtf =>
        val sd = d.completedCtf.copy(metaId = Option(d.metaId.id))

        import scala.concurrent.ExecutionContext.Implicits.global
        // left => existing ID
        // right => new ID
        val pushCtfResult: Future[Either[scala.xml.Elem, Option[scala.xml.Elem]]] = for {
          existingDuel <- persister.getSimilarCtf(sd)
          result <- existingDuel match {
            case Some(existingId) => Future(Left(existingId))
            case None =>
              // try to push three times
              val tryToPush = persister.pushCtf(sd) recoverWith {
                case e => persister.pushCtf(sd)
              } recoverWith {
                case e => persister.pushCtf(sd)
              }
              for {
                _ <- tryToPush
                haveIt <- persister.getSimilarCtf(sd)
              } yield Right(haveIt)
          }
        } yield result
        pushCtfResult onFailure {
          case f => log.error(f, "Failed to publish ctf result")
        }
        for {
          Right(Some(newCtf)) <- pushCtfResult
        } {
          self ! NewlyAddedCtf(newCtf)
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
      demoLoader.foreach(_ ! g)
      context.parent ! g
    case g: MetaCompletedCtf =>
      completedDuelPublish ! g
      demoLoader.foreach(_ ! g)
      context.parent ! g
    case dd: DemoDownloaded =>
      context.parent ! dd
      hazelcast.getTopic[String]("downloaded-demos").publish(dd.gameId)
    case m: IterationMetaData =>
      context.parent ! m
    case RotateMeta =>
      metaContext ! RotateMeta
    case nd: NewlyAddedDuel =>
      context.parent ! nd
    case nd: NewlyAddedCtf =>
      context.parent ! nd
    case 1 => println("yay")
  }

}
object Woot {

  case class LiveDuelUpdated(server: Server, liveDuelO: Option[LiveDuel])

  def props(hazelcast: HazelcastInstance,
             persister: WSAsyncGamePersister, journalGenerator: JournalGenerator, disableHashing: Boolean, saveDemosToO: Option[File]) = {
    Props(new Woot(hazelcast, persister, journalGenerator, disableHashing, saveDemosToO))
  }

  case class MetaCompletedDuel(metaId: IterationMetaData, completedDuel: SimpleCompletedDuel)
  case class MetaCompletedCtf(metaId: IterationMetaData, completedCtf: SimpleCompletedCTF)

  case object RotateMeta

  case class JournalGenerator
  (
  generate: IterationMetaData => JournalBytes.Writer
    )
case class NewlyAddedDuel(duel: scala.xml.Elem)
case class NewlyAddedCtf(ctf: scala.xml.Elem)
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