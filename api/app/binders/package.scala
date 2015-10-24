import java.time.ZonedDateTime

import gg.duel.query._
import play.api.mvc.{QueryStringBindable, PathBindable}

import scala.util.Try

/**
 * Created on 04/10/2015.
 */
package object binders {

  object dateTime {
    def unapply(input: String): Option[ZonedDateTime] = {
      Try(ZonedDateTime.parse(input)).toOption
    }
  }

  implicit def gameIdPathBindable(implicit bindableStringPath: PathBindable[String]): PathBindable[GameId] =
    new PathBindable[GameId] {
      override def unbind(key: String, value: GameId): String = ???
      override def bind(key: String, value: String): Either[String, GameId] = value match {
        case dateTime(zdt) => Right(GameId(zdt))
        case _ => Left("Invalid game ID supplied")
      }
    }

  implicit def multipleByIdQueryBinder(implicit queryStringBindable: QueryStringBindable[String]): QueryStringBindable[MultipleByIdQuery] =
    new QueryStringBindable[MultipleByIdQuery] {
      override def unbind(key: String, value: MultipleByIdQuery): String = ???
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, MultipleByIdQuery]] = {
        params.get(key).map { games =>
          games.collect {
            case dateTime(zdt) => GameId(zdt)
          }.toSet match {
            case set if set.nonEmpty =>
              Right(MultipleByIdQuery(set))
            case _ =>
              Left("Expected at least one valid ID.")
          }
        }
      }
    }

  implicit def conditionBindable(implicit bindableStringPath: PathBindable[String]): PathBindable[TimingCondition] =
    new PathBindable[TimingCondition] {
      override def bind(key: String, value: String): Either[String, TimingCondition] = {
        val matchDate = s"""(until|to|from|after)/(.*)/""".r
        value match {
          case matchDate(str, dateTime(zdt)) => Right {
            str match {
              case "until" => Until(zdt)
              case "to" => To(zdt)
              case "after" => After(zdt)
              case "from" => From(zdt)
            }
          }
          case "recent/" => Right(Recent)
          case "first/" => Right(First)
          case _ => Left(s"Unfamiliar value: $value")
        }
      }

      override def unbind(key: String, value: TimingCondition): String = ???
    }
}
