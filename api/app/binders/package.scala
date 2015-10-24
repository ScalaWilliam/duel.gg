import controllers.PlayerCondition
import gg.duel.query._
import play.api.mvc.{PathBindable, QueryStringBindable}

/**
 * Created on 04/10/2015.
 */
package object binders {

  implicit def gameIdPathBindable(implicit bindableStringPath: PathBindable[String]): PathBindable[GameId] =
    new PathBindable[GameId] {
      override def unbind(key: String, value: GameId): String = ???
      override def bind(key: String, value: String): Either[String, GameId] = value match {
        case zdt => Right(GameId(zdt))
        case _ => Left("Invalid game ID supplied")
      }
    }

  implicit def playerConditionQueryBinder(implicit queryStringBindable: QueryStringBindable[String]): QueryStringBindable[PlayerCondition] = {
    new QueryStringBindable[PlayerCondition] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, PlayerCondition]] = {
        Option {
          Right {
            PlayerCondition(
              player = params.get("player").toSet.flatten,
              user = params.get("user").toSet.flatten,
              clan = params.get("clan").toSet.flatten
            )
          }
        }
      }

      override def unbind(key: String, value: PlayerCondition): String = ???
    }
  }

  implicit def multipleByIdQueryBinder(implicit queryStringBindable: QueryStringBindable[String]): QueryStringBindable[MultipleByIdQuery] =
    new QueryStringBindable[MultipleByIdQuery] {
      override def unbind(key: String, value: MultipleByIdQuery): String = ???
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, MultipleByIdQuery]] = {
        params.get(key).map { games =>
          games.collect {
            case zdt => GameId(zdt)
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
          case matchDate(str, time) => Right {
            str match {
              case "until" => Until(time)
              case "to" => To(time)
              case "after" => After(time)
              case "from" => From(time)
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
