import java.io.File

import akka.actor.ActorSystem
import gg.duel.pinger.Pipeline
import gg.duel.pinger.analytics.duel.SimpleCompletedDuel
import gg.duel.pinger.app.Woot.{MetaCompletedDuel, MetaCompletedCtf}
import gg.duel.pinger.service.{WSAsyncGamePersister, DemoLoader}

object DemoLoaderApp extends App {
  implicit val as = ActorSystem("loadMe")
  val demosDir = new File("demos")

  demosDir.mkdirs()
  val asyncCtfPersister = new WSAsyncGamePersister(Pipeline.pipeline, "http://odin.duel.gg:8984", "db-stage", "wattqw")
  import akka.actor.ActorDSL._
  val paccy = actor(new Act {
    val accy = context.actorOf(DemoLoader.props(demosDir, asyncCtfPersister))
    // 2015-01-24T19:39:59Z::188.226.136.111:20000
    whenStarting {
      accy ! MetaCompletedDuel(null, SimpleCompletedDuel("2015-01-24T19:39:59Z::188.226.136.111:20000", 1, null, null, 2, null, null, null, null, null, null))
    }
    become {
      case any =>
        println(any)
    }

  })
  as.awaitTermination(
  )
  as.shutdown()
}