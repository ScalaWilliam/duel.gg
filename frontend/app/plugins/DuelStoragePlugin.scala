package plugins

import akka.actor.FSM.Failure
import akka.actor.{ActorRef, ActorLogging, Status, Kill}
import akka.util.Timeout
import com.hazelcast.core.{Message, MessageListener}
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
import plugins.DuelStoragePlugin.StorageEvents.{DuelUpdated, UserAvailableInSearch, UserUpdated}
import plugins.DuelStoragePlugin.{DuelStorage, User, Duel}
import scala.annotation.tailrec
import scala.async.Async.{async, await}
import play.api._
import plugins.DataSourcePlugin._
import scala.collection.immutable.SortedMap
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.util.Try
import scala.xml.{PCData, Text}

object DuelStoragePlugin {

  def plugin: DuelStoragePlugin = Play.current.plugin[DuelStoragePlugin]
    .getOrElse(throw new RuntimeException("DataSourcePlugin plugin not loaded"))

  case class Player(id: String)

  case class Duel(id: Int, dateTime: Long, users: Set[String], nicknames: Set[String], jsonData: String)

  object Duel { def fromXml(elem: scala.xml.Elem) = Duel((elem \@ "id").toInt, (elem \@ "at").toLong, (elem \@ "users").split(" ").toSet, (elem \@ "nicknames").split(" ").toSet, elem \@ "json") }

  implicit class containsIns(val x: String) extends AnyVal {
    def containsInsensitive(v: String) = x.toLowerCase contains v.toLowerCase
  }

  case class User(id: String, name: String, nickname: String, nicknames: Set[String]) {
    def asBasicJson = Json.toJson(Map("id" -> id, "name" -> name, "nickname" -> nickname))
  }

  object User {
    def fromXml(elem: scala.xml.Elem) = User(id = elem \@ "id", name = elem \@ "name", nickname = elem \@ "game-nickname", nicknames = (elem \ "nickname").map(_.text).toSet)
  }
  object DuelStorage {
    def empty = DuelStorage(Map.empty, SortedMap.empty[Int, Duel])
  }
  case class DuelStorage(usersList: Map[String, User], duelIdToDuel: SortedMap[Int, Duel]) {
    def createSliders(input: Vector[Int]): Map[Int, Vector[Int]] = {
      if ( input.size < 9 ) {
        (1 to input.size).map(n => input(n-1) -> input).toMap
      } else {
        ((1 to 4).map(n => input(n-1) -> input.take(9)) ++ input.sliding(9).toList.map(x => x(4) -> x) ++ ((input.size - 3) to input.size).map(n => input(n-1) -> input.takeRight(9))).toMap
      }
    }
    def stat = s"""# users: ${usersList.size}, # duels: ${duelIdToDuel.size}"""
    def withExtraGames(duels: Vector[Duel]) = {
      copy(duelIdToDuel = duelIdToDuel ++ duels.map(g => g.id -> g))
    }
    def withExtraUsers(users: Vector[User]) = {
      copy(usersList = usersList ++ users.map(u => u.id -> u))
    }
    val duelszMap = scala.collection.immutable.IntMap.apply(duelIdToDuel.mapValues(_.jsonData).toList :_*)
    val users = usersList.mapValues(_.nicknames.toList)
    val sortedDuelIds = duelIdToDuel.valuesIterator.toVector.sortBy(-_.dateTime).map(_.id)
    val sortedDuelIdsPerNickname = duelIdToDuel.valuesIterator.flatMap{g => g.nicknames.map(n => n -> g.id)}.toVector.groupBy(_._1).mapValues(_.map(_._2).toVector)
    val slidingDuels = createSliders(sortedDuelIds)
    val slidingPerUsername = {
      users.mapValues(_.flatMap(sortedDuelIdsPerNickname.get).flatten.sortBy(sortedDuelIds.indexOf).toVector).mapValues(createSliders)
    }
    val duelsPerUsername = {
      users.mapValues(_.flatMap(sortedDuelIdsPerNickname.get).flatten.sortBy(sortedDuelIds.indexOf).toVector)
    }
    val getMain = sortedDuelIds.take(16).map(duelszMap)
    def getMainBefore(duelId: Int) = sortedDuelIds.dropWhile(_ != duelId).drop(1).take(16).map(duelszMap)
    def getMainAfter(duelId: Int) = sortedDuelIds.takeWhile(_ != duelId).takeRight(16).map(duelszMap)
    def getNickDuels(nickname: String) = sortedDuelIdsPerNickname.get(nickname).map(_.take(9).map(duelszMap))
    def getUserDuels(userId: String) = duelsPerUsername.get(userId).map(_.take(9).map(duelszMap))
    def getUserDuelsFocus(userId: String, duelId: Int) = slidingPerUsername.get(userId).flatMap(_.get(duelId).map(_.map(duelszMap)))
    def getUserDuelsAfter(userId: String, duelId: Int) = duelsPerUsername.get(userId).map(_.takeWhile(_ != duelId).takeRight(9).map(duelszMap))
    def getUserDuelsBefore(userId: String, duelId: Int) = duelsPerUsername.get(userId).map(_.dropWhile(_ != duelId).drop(1).take(9).map(duelszMap))
    def getDuelFocus(duelId: Int) = slidingDuels.get(duelId).map(_.map(duelszMap))
    def search(term: String, beforeDuelId: Option[Int]) = {
      val matchingUsers = for {
        (u, ns) <- users
        if (u containsInsensitive term) || ns.exists(_ containsInsensitive term)
      } yield u -> ns
      val matchingNicks = for {
        nick <- sortedDuelIdsPerNickname.keySet
        if (nick containsInsensitive term) || matchingUsers.exists(_._2.contains(nick))
      } yield nick
      val userNames = matchingUsers.keySet
      val userNicks = matchingUsers.values.flatten.toSet
      val showOnlyNicks = matchingNicks diff userNicks
      val showOnlySortedNicks = showOnlyNicks.toList.filter(sortedDuelIdsPerNickname(_).size>=10).sortBy(sortedDuelIdsPerNickname(_).size).reverse
      val matchingDuelIds = (matchingNicks ++ userNicks).map(sortedDuelIdsPerNickname).flatten
      val matchingDuels = beforeDuelId match {
        case None => sortedDuelIds.filter(matchingDuelIds.contains).take(9).map(duelszMap)
        case Some(did) => sortedDuelIds.dropWhile(_ != did).drop(1).filter(matchingDuelIds.contains).map (duelszMap).take(9)
      }
      (matchingUsers, showOnlySortedNicks, matchingDuels)
    }
    def duelsMissingUsers = {
      val duelIds = for {
        (userId, duels) <- duelsPerUsername
        duelId <- duels
        duel <- duelIdToDuel.get(duelId)
        if !(duel.users contains userId)
      } yield duelId
      duelIds.toSet
    }
    def duelsMissingUser(userId: String) = {
      val ids = for {
        duels <- duelsPerUsername.get(userId).toVector
        duelId <- duels
        duel <- duelIdToDuel.get(duelId)
        if !(duel.users contains userId)
      } yield duelId
      ids.toSet
    }
  }

  object StorageEvents {
    case class UserUpdated(userId: String)
    case class DuelUpdated(gameId: Int)
    case class UserAvailableInSearch(userId: String)
    case class DuelAvailableInSearch(duelId: Int)
    case object Reload
    case object UserRecompile
    case object CompileUncompiled
    case class MergedGamesReceived(duels: Vector[Duel])
  }
}

class DuelStoragePlugin(implicit app: Application) extends Plugin {
  @volatile var currentStorage: DuelStorage = DuelStorage.empty
  implicit lazy val as = Akka.system
  import akka.actor.ActorDSL._
  import akka.pattern.pipe
  case class WaitForUser(userId: String)
  lazy val hazelcastTopic = HazelcastPlugin.hazelcastPlugin.hazelcast.getTopic[String]("new-duels")
  lazy val listenerId = hazelcastTopic.addMessageListener(new MessageListener[String] {
    override def onMessage(message: Message[String]): Unit = {
      try { controllerActor ! DuelUpdated(message.getMessageObject.toInt) }
      catch { case _ => }
    }
  })
  lazy val recordsUserRecords = actor(new Act {
    whenStarting {
      context.system.eventStream.subscribe(self, classOf[UserAvailableInSearch])
      become(waiting(Vector.empty))
    }
    def waiting(users: Vector[(String, ActorRef)]): Receive = {
      case msg @ UserAvailableInSearch(userId) =>
        val (sendTo, others) = users.partition(_._1 == userId)
        become(waiting(others))
        sendTo.map(_._2).foreach(_ ! msg)
      case WaitForUser(userId) =>
        become(waiting(users :+ userId -> sender()))
    }
  })
  def createProfile(userId: String): Future[Unit] = {
    controllerActor ! UserUpdated(userId)
    import ExecutionContext.Implicits.global
    import akka.pattern.ask
    import concurrent.duration._

    implicit val timeout = Timeout(1.minute)
    recordsUserRecords.ask(WaitForUser(userId)).mapTo[UserAvailableInSearch].map(_ => Unit)
  }
  lazy val controllerActor = actor(name = "duel-storage-controller")(new Act with ActWithStash with ActorLogging {
    import plugins.DuelStoragePlugin.StorageEvents._
    case object InitialMaterialisationFinished
    case class InitialDataLoaded(duelStorage: DuelStorage)
    case class NewStorageReceived(duelStorage: DuelStorage)
    case class NewStorageReceivedWithUpdateTheseDuelsPlease(duelStorage: DuelStorage, duels: Set[Int])
    case object CheckDuelsMissingUsers
    import ExecutionContext.Implicits.global

    whenStarting {
      log.info("Starting materialisation of unmaterialised duels...")
      DataSourcePlugin.plugin.materialiseUnmaterialisedGames.map(_ => InitialMaterialisationFinished) pipeTo self

//      self ! DuelRegistered(389639368)
//      self ! DuelRegistered(389639368)
//      self ! DuelRegistered(38963932)
//      self ! UserUpdated("drakas")
//      self ! UserUpdated("ahuwehuas")
    }

    becomeStacked(waitingForInitialMaterialisation)

    def waitingForInitialMaterialisation: Receive = {
      case akka.actor.Status.Failure(reason) => throw new RuntimeException("Failed to materialise unmaterialised: $reason", reason)
      case _: DuelUpdated => stash()
      case _: UserUpdated => stash()
      case InitialMaterialisationFinished =>
        log.info("Materialised unmaterialised games.")
        val usersR = DataSourcePlugin.plugin.queryUsers
        val queryGamesR = DataSourcePlugin.plugin.gamesZ
        log.info("Waiting for the initial loading.")
        becomeStacked(waitingForInitialLoading)
        async {
          val firstStorage = currentStorage.withExtraUsers(await(usersR)).withExtraGames(await(queryGamesR))
          val updateDuelIds = firstStorage.duelsMissingUsers
          val nextStorage = if (updateDuelIds.nonEmpty) {
            log.info(s"Updating duels as user IDs out of sync: $updateDuelIds")
            await(DataSourcePlugin.plugin.materialiseGames(updateDuelIds))
            firstStorage.withExtraGames(await(DataSourcePlugin.plugin.gamesZ))
          } else {
            log.info(s"No duels need to re-materialise at the start.")
            firstStorage
          }
          InitialDataLoaded(nextStorage)
        } pipeTo self
    }

    def waitingForInitialLoading: Receive = {
      case InitialDataLoaded(cs) =>
        log.info(s"Initial data loaded: ${cs.stat}")
        currentStorage = cs
        log.info("Switching to real time updates mode.")
        becomeStacked(waitingForRealTimeUpdates)
        unstashAll()
      case _: DuelUpdated => stash()
      case _: UserUpdated => stash()
      case akka.actor.Status.Failure(reason) =>
        throw new RuntimeException(s"Failed to load initial data due to $reason", reason)
    }

    def waitingForRealTimeUpdates: Receive = {
      case uu @ UserUpdated(userId) =>
        log.info(s"Received user update $uu. Waiting for update.")
        becomeStacked(waitingForUpdatedUser(userId))
        async {
          val newUser = await(DataSourcePlugin.plugin.queryUser(userId))
          newUser match {
            case Some(user) =>
              val newerStorage = currentStorage.withExtraUsers(Vector(user))
              val duelsMissingThisUser = newerStorage.duelsMissingUser(userId)

              log.info(s"These duels will be updated due to user $userId: $duelsMissingThisUser")
              async {
                log.info(s"Materialising games $duelsMissingThisUser...")
                await(DataSourcePlugin.plugin.materialiseGames(duelsMissingThisUser))
                val newGames = await(DataSourcePlugin.plugin.queryGames(duelsMissingThisUser))
                MergedGamesReceived(newGames)
              } pipeTo self

              NewStorageReceived(newerStorage)
            case None => throw new IllegalStateException(s"Expected user $userId to exist, none found.")
          }
        } pipeTo self

      case dr@DuelUpdated(duelId) =>
        log.info(s"Received duel $dr. Waiting for update.")
        becomeStacked(waitingForUpdatedDuel(duelId))
        async {
          log.info(s"Materialising game $duelId...")
          await(DataSourcePlugin.plugin.materialiseGames(Set(duelId)))
          val newGame = await(DataSourcePlugin.plugin.queryGame(duelId))
          newGame match {
            case Some(game) => NewStorageReceived(currentStorage.withExtraGames(Vector(game)))
            case None => throw new IllegalStateException(s"Expected game $duelId to exist, none found.")
          }
        } pipeTo self

      case MergedGamesReceived(games) =>
        log.info(s"Found games to update: ${games.map(_.id).toSet}")
        currentStorage = currentStorage.withExtraGames(games)
    }
    def waitingForUpdatedUser(userId: String): Receive = {
      case NewStorageReceived(cs) =>
        log.info(s"Duel Update: Received new storage due to user $userId: ${cs.stat}")
        currentStorage = cs
        context.system.eventStream.publish(UserAvailableInSearch(userId))
        unbecome()
        unstashAll()
      case akka.actor.Status.Failure(reason) =>
        log.error(reason, s"Failed to update stuff for user $userId.")
        log.info("Resuming...")
        unbecome()
        unstashAll()
      case _: UserUpdated => stash()
      case _: DuelUpdated => stash()
      case _: MergedGamesReceived => stash()
    }
    def waitingForUpdatedDuel(duelId: Int): Receive = {
      case NewStorageReceived(cs) =>
        log.info(s"Duel Update: Received new storage due to duel $duelId: ${cs.stat}")
        currentStorage = cs
        context.system.eventStream.publish(DuelAvailableInSearch(duelId))
        unbecome()
        unstashAll()
      case akka.actor.Status.Failure(reason) =>
        log.error(reason, s"Failed to update stuff for duel $duelId")
        log.info("Resuming...")
        unbecome()
        unstashAll()
      case _: UserUpdated => stash()
      case _: DuelUpdated => stash()
      case _: MergedGamesReceived => stash()
    }
  })
  override def onStart(): Unit = {
    // what affects the results?
    // * new users
    // * user updates
    // * new games
    // * manual refresh
    // * that's about it! :-)
    controllerActor
    recordsUserRecords
    listenerId
  }
  override def onStop(): Unit = {
    controllerActor ! Kill
    hazelcastTopic.removeMessageListener(listenerId)
  }
}