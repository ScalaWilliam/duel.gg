package models

import java.time.ZonedDateTime

/**
 * Created on 14/08/2015.
 */

case class Users(users: Map[UserId, FullUser]) {

  def withUser(userId: UserId, fullUser: FullUser) = copy(users = users.updated(userId, fullUser))

  /** todo add idempotency **/
  def withRegisterUser(registerUser: RegisterUser, dateTime: ZonedDateTime): Either[String, (Users, FullUser)] = {
    def userExists = users.contains(UserId(registerUser.id))
    def googleExists = users.exists(_._2.google == registerUser.google)
    def nicknameInUse = users.exists(_._2.nicknames.exists(_.nickname == registerUser.nickname.nickname))
    if ( userExists ) Left("User already exists")
    else if ( googleExists ) Left("User e-mail already in use")
    else if ( nicknameInUse ) Left("Nickname already in use")
    else Right {
      val fullUser = registerUser.toFull(dateTime)
      val newUsers = withUser(
        userId = UserId(registerUser.id),
        fullUser = fullUser
      )
      newUsers -> fullUser
    }
  }

  def withNickname(userId: UserId, setNickname: SetNickname, dateTime: ZonedDateTime): Either[String, (Users, FullUser)] = {

    def nicknameInUseByAnotherUser = {
      users.exists {
        case (`userId`, _) => false
        case (otherUserId, otherUser) =>
          otherUser.nicknames.exists(_.nickname == setNickname.nickname)
      }
    }

    users.get(userId) match {
      case None =>
        Left(s"User id $userId does not exist")
      case Some(user) if nicknameInUseByAnotherUser =>
        Left(s"Nickname has already been used by another user.")
      case Some(user) =>
        Right {
          val newUser = user.withNewNickname(setNickname, dateTime)
          withUser(userId, newUser) -> newUser
        }
    }
  }
}
