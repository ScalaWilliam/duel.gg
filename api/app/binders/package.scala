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
              clan = params.get("clan").toSet.flatten,
              playerConditionOperator  = params.get("operator").toList.flatten.collectFirst{ case PlayerConditionOperator(operand) => operand }.getOrElse(Or)
            )
          }
        }
      }

      override def unbind(key: String, value: PlayerCondition): String = ???
    }
  }
  implicit def limitConditionPathBindable(implicit intBindable: QueryStringBindable[Int]): QueryStringBindable[LimitCondition] = {
    new QueryStringBindable[LimitCondition] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LimitCondition]] = {
        intBindable.bind(key, params).map(_.right.map(SpecificLimit)).orElse(Option(Right(DefaultLimit)))
      }

      override def unbind(key: String, value: LimitCondition): String = ???
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


  implicit def focusBinder(implicit intBindable: QueryStringBindable[Int]): QueryStringBindable[Focus] =
    new QueryStringBindable[Focus] {
      override def unbind(key: String, value: Focus): String = ???
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Focus]] = {
        def bindInt(keyName: String) = QueryStringBindable.bindableInt.bind(key = keyName, params = params)

        PartialFunction.condOpt(List("radius", "next", "previous").map(bindInt)) {
          case List(Some(Right(radius)), _, _) if radius <= 50 =>
            RadialFocus(radius)
          case List(_, Some(Right(next)), Some(Right(previous))) if next <= 25 && previous <= 25 =>
            AsymmetricFocus(
              next = next,
              previous = previous
            )
        } match {
            case Some(focus) => Option(Right(focus))
            case None => Option(Right(SimpleFocus))
          }
        }
      }


  implicit def conditionBindable(implicit bindableStringPath: PathBindable[String]): PathBindable[TimingCondition] =
    new PathBindable[TimingCondition] {
      override def bind(key: String, value: String): Either[String, TimingCondition] = {
        value match {
          case TimingCondition(timingCondition) => Right(timingCondition)
          case _ => Left(s"Unfamiliar value: $value")
        }
      }

      override def unbind(key: String, value: TimingCondition): String = ???
    }
  implicit def gameTypeBindable(implicit bindableStringPath: PathBindable[String]): PathBindable[GameType] =
    new PathBindable[GameType] {
      override def bind(key: String, value: String): Either[String, GameType] = {
        value match {
          case GameType(gameType) => Right(gameType)
          case _ => Left(s"Unfamiliar value: $value")
        }
      }
      override def unbind(key: String, value: GameType): String = ???
    }
}
