package models.lookups

import models.{FullUser, Users}

/**
 * Created on 14/08/2015.
 */

case class ByGoogle(google: String) extends UserLookup {
  override def matches(fullUser: FullUser): Boolean =
    fullUser.google == google
}


sealed trait UserLookup {
  def unapply(users: Users): Option[FullUser] = {
    users.users.collectFirst {
      case (userId, user) if matches(user) => user
    }
  }
  def matches(fullUser: FullUser): Boolean
}

case class ByNickname(nickname: String) extends UserLookup {
  override def matches(fullUser: FullUser): Boolean =
    fullUser.nicknames.exists(n => n.nickname == nickname)
}
case class ByNicknameCountryCode(nickname: String, countryCode: String) extends UserLookup {
  override def matches(fullUser: FullUser): Boolean =
    fullUser.nicknames.exists(un => un.nickname == nickname && un.countryCode == countryCode)
}
