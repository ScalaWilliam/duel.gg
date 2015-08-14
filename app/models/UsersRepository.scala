package models

import java.time.ZonedDateTime
import javax.inject._

import akka.agent.Agent
import play.api.libs.iteratee.Concurrent

import scala.async.Async
import scala.concurrent.{ExecutionContext, Future}



@Singleton
class UsersRepository @Inject()()(implicit executionContext: ExecutionContext) {

  val (userUpdatesEnum, userUpdatesChannel) = Concurrent.broadcast[FullUser]

  val usersAgent = Agent(Users(users = Map(UserId("drakas") -> FullUser.example)))

  def users: Users = usersAgent.get()

  def registerUser(registerUser: RegisterUser): Future[Either[String, FullUser]] = {
    val dateTime = now()
    def getUserById = users.users.get(UserId(registerUser.id))
    val startUser = getUserById
    Async.async {
      users.withRegisterUser(registerUser, dateTime) match {
        case Left(reason) =>
          Left(reason)
        case Right((_, newUser)) =>
          Async.await(usersAgent.alter(_.withUser(UserId(registerUser.id), newUser)))
          val endUser = getUserById
          if ( endUser != startUser ) {
            userUpdatesChannel.push(newUser)
          }
          Right(newUser)
      }
    }
  }

  def now() = ZonedDateTime.now().withNano(0)

  /** Return a failure or the user after update application **/
  def setNickname(userId: UserId, setNickname: SetNickname): Future[Either[String, FullUser]] = {
    def getUserById = users.users.get(userId)
    val startUser = getUserById
    val dateTime = now()
    Async.async {
      users.withNickname(userId, setNickname, dateTime) match {
        case Right((_, updatedUser)) =>
          Async.await(usersAgent.alter(_.withUser(userId, updatedUser)))
          val endUser = getUserById
          if ( startUser != endUser ) {
            userUpdatesChannel.push(updatedUser)
          }
          Right(updatedUser)
        case Left(reason) => Left(reason)
      }
    }
  }
}
