package controllers

import models.lookups.{ByGoogle, ByNickname, ByNicknameCountryCode, UserLookup}
import models._
import play.api.mvc.{QueryStringBindable, PathBindable}

/**
 * Created on 14/08/2015.
 */
object UserLookupBinders {

  implicit def userIdBindable: PathBindable[UserId] =
    new PathBindable[UserId] {
      override def unbind(key: String, value: UserId): String = ???
      override def bind(key: String, value: String): Either[String, UserId] =
        UserId.fromString(value) match {
          case Some(stuff) => Right(stuff)
          case None => Left("User ID invalid.")
        }
    }

  implicit def ulQSB(implicit stringBindable: QueryStringBindable[String]): QueryStringBindable[UserLookup] =
    new QueryStringBindable[UserLookup] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, UserLookup]] = {
        def google = stringBindable.bind("google", params).map(_.right.map(ByGoogle.apply))
        def byNickname = stringBindable.bind("nickname", params).map(_.right.map(ByNickname.apply))
        def byNicknameCountryCode = for {
          n <- stringBindable.bind("nickname", params)
          c <- stringBindable.bind("country-code", params)
        } yield for {nv <- n.right; cv <- c.right} yield ByNicknameCountryCode(nv, cv)
        byNicknameCountryCode orElse byNickname orElse google
      }

      override def unbind(key: String, value: UserLookup): String = ???
    }
}
