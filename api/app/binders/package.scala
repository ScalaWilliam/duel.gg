import gg.duel.query._
import play.api.mvc.{PathBindable, QueryStringBindable}

/**
  * Created on 04/10/2015.
  */
package object binders {

  implicit object queryConditionQueryStringBindable extends QueryStringBindable[QueryCondition] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QueryCondition]] = {
      Option(QueryCondition.apply(params))
    }

    override def unbind(key: String, value: QueryCondition): String = {
      {
        for {(k, values) <- value.toMap.toList
             value <- values}
          yield QueryStringBindable.bindableString.unbind(
            key = k,
            value = value
          )
      }.mkString("&")
    }
  }

  implicit object timingConditionPathBindable extends PathBindable[TimingCondition] {
    override def bind(key: String, value: String): Either[String, TimingCondition] =
      value match {
        case TimingCondition(condition) => Right(condition)
        case c => Left(s"Unknown timing condition $c")
      }

    override def unbind(key: String, value: TimingCondition): String = value.stringValue
  }
  implicit object lookupDirectionBindable extends PathBindable[LookupDirection] {
    override def bind(key: String, value: String): Either[String, LookupDirection] =
      value match {
        case LookupDirection(ld) => Right(ld)
        case c => Left(s"Unknown lookup direction $c")
      }

    override def unbind(key: String, value: LookupDirection): String = value.stringValue
  }

  implicit def gameIdPathBindable: PathBindable[GameId] =
    new PathBindable[GameId] {
      override def unbind(key: String, value: GameId): String =
        PathBindable.bindableString.unbind(key, value.gameId)

      override def bind(key: String, value: String): Either[String, GameId] =
        Right(GameId(value))
    }

  implicit def limitConditionPathBindable: QueryStringBindable[LimitCondition] = {
    new QueryStringBindable[LimitCondition] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LimitCondition]] = {
        QueryStringBindable.bindableInt.bind(key, params).map(_.right.map(SpecificLimit)).orElse(Option(Right(DefaultLimit)))
      }

      override def unbind(key: String, value: LimitCondition): String =
        value match {
          case DefaultLimit => ""
          case SpecificLimit(n) => QueryStringBindable.bindableInt.unbind(key, n)
        }
    }
  }

  implicit def multipleByIdQueryBinder: QueryStringBindable[MultipleByIdQuery] =
    new QueryStringBindable[MultipleByIdQuery] {
      override def unbind(key: String, value: MultipleByIdQuery): String =
        value.gameIds.toList.map(v => QueryStringBindable.bindableString.unbind(key, v.gameId)).mkString("&")

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

}
