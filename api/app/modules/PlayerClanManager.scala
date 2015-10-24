package modules

import java.time.ZonedDateTime
import javax.inject._

import akka.agent.Agent
import gg.duel.uservice.clan.{RegisterClan, SetPatterns}
import gg.duel.uservice.clanplayer.{ClanAndClanPlayers, PlayerAndClan, ClanPlayerSystem}
import gg.duel.uservice.player.{RegisterPlayer, SetNickname}

import scala.async.Async
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created on 27/08/2015.
 */
@Singleton
class PlayerClanManager @Inject()(playerClanPersistence: PlayerClanPersistence)(implicit executionContext: ExecutionContext) {

  implicit class agentExtension[T](agent: Agent[T]) {
    /** Do stuff on it and then return after changes have been made. f has to be pure. **/
    def alterWithResult[V](f: T => (T, V)): Future[(T, V)] = {
      Async.async {
        val initialValue = agent.get()
        Async.await(agent.alter(f.andThen(_._1)))
        f(initialValue)
      }
    }
  }

  def now(): ZonedDateTime = ZonedDateTime.now()

  val clanPlayerSystemAgent = Agent(ClanPlayerSystem(
    clans = playerClanPersistence.getClans,
    players = playerClanPersistence.getPlayers
  ))

  def cps = clanPlayerSystemAgent.get()

  def setNickname(playerId: String, setNickname: SetNickname, currentTime: ZonedDateTime):
  Future[Option[Either[String, PlayerAndClan]]] = {
    Async.async {
      Async.await {
        clanPlayerSystemAgent.alterWithResult { system =>
          import system._
          system.players.get(playerId) match {
            case Some(player) =>
              player.setNickname(setNickname, currentTime) match {
                case stuff@Right((newSystem, _)) => newSystem -> Option(stuff)
                case stuff => system -> Option(stuff)
              }
            case None => system -> Option.empty
          }
        }
      } match {
        case (_, None) => None
        case (_, Some(Left(reason))) => Some(Left(reason))
        case (_, Some(Right((system, player)))) =>
          playerClanPersistence.putPlayer(player)
          import system._
          Option(Right(player.withClans))
      }
    }
  }

  def setPatterns(clanId: String, setPatterns: SetPatterns, currentTime: ZonedDateTime):
  Future[Option[Either[String, ClanAndClanPlayers]]] = {
    Async.async {
      Async.await {
        clanPlayerSystemAgent.alterWithResult { system =>
          import system._
          system.clans.get(clanId) match {
            case Some(clan) =>
              clan.setPatterns(setPatterns, currentTime) match {
                case stuff@Right((newSystem, _)) => newSystem -> Option(stuff)
                case stuff => system -> Option(stuff)
              }
            case None => system -> Option.empty
          }
        }
      } match {
        case (_, None) => None
        case (_, Some(Left(reason))) => Some(Left(reason))
        case (_, Some(Right((system, clan)))) =>
          playerClanPersistence.putClan(clan)
          import system._
          Option(Right(clan.withPlayers))
      }
    }
  }

  def registerPlayer(registerPlayer: RegisterPlayer, registerTime: ZonedDateTime): Future[Either[String, PlayerAndClan]] = {
    Async.async {
      Async.await {
        clanPlayerSystemAgent.alterWithResult { system =>
          system.registerPlayerW(registerPlayer, registerTime) match {
            case r@Right((newSystem, newPlayer)) =>
              newSystem -> r
            case Left(failure) =>
              system -> Left(failure)
          }
        }
      } match {
        case (newSystem, either) =>
          either.right.foreach { case (_, player) =>
            playerClanPersistence.putPlayer(player)
          }
          either.right.map { case (system, player) =>
            import system._
            player.withClans
          }
      }
    }
  }

  def registerClan(registerClan: RegisterClan, registerTime: ZonedDateTime): Future[Either[String, ClanAndClanPlayers]] = {
    Async.async {
      Async.await {
        clanPlayerSystemAgent.alterWithResult { system =>
          system.registerClanW(registerClan, registerTime) match {
            case r@Right((newSystem, newClan)) =>
              newSystem -> r
            case Left(failure) =>
              system -> Left(failure)
          }
        }
      } match {
        case (newSystem, either) =>
          either.right.foreach { case (_, clan) =>
            playerClanPersistence.putClan(clan)
          }
          either.right.map { case (system, clan) =>
            import system._
            clan.withPlayers
          }
      }
    }
  }
}