package services
import javax.inject._

import modules.GamesManager

import scala.async.Async
import scala.concurrent.ExecutionContext

/**
 * Created by William on 27/10/2015.
 */
@Singleton
class LoadJournalledIntoCore @Inject()(gamesManager: GamesManager, readJournalledService: ReadJournalledService)
                                      (implicit executionContext: ExecutionContext){

  Async.async {
    Async.await(readJournalledService.parsedGamesFuture).foreach {
      case Left(duel) => gamesManager.addDuel(duel)
      case Right(ctf) => gamesManager.addCtf(ctf)
    }
  }

}
