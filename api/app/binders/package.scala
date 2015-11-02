import gg.duel.query._
import play.api.mvc.{PathBindable, QueryStringBindable}

/**
 * Created on 04/10/2015.
 */
package object binders {

  implicit def gameIdPathBindable: PathBindable[GameId] =
    new PathBindable[GameId] {
      override def unbind(key: String, value: GameId): String = PathBindable.bindableString.unbind(key, value.gameId)
      override def bind(key: String, value: String): Either[String, GameId] = value match {
        case zdt => Right(GameId(zdt))
        case _ => Left("Invalid game ID supplied")
      }
    }

  implicit def playerConditionQueryBinder: QueryStringBindable[PlayerCondition] = {
    new QueryStringBindable[PlayerCondition] {

      val playerParam = "player"
      val userParam = "user"
      val clanParam = "clan"
      val operatorParam = "operator"

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, PlayerCondition]] = {
        Option {
          Right {
            PlayerCondition(
              player = params.get(playerParam).toSet.flatten,
              user = params.get(userParam).toSet.flatten,
              clan = params.get(clanParam).toSet.flatten,
              playerConditionOperator  = params.get(operatorParam).toList.flatten.collectFirst{ case PlayerConditionOperator(operand) => operand }.getOrElse(Or)
            )
          }
        }
      }

      override def unbind(key: String, value: PlayerCondition): String = {
        value.player.toList.map(player => QueryStringBindable.bindableString.unbind(playerParam, player)) ++
          value.user.toList.map(user => QueryStringBindable.bindableString.unbind(userParam, user)) ++
          value.clan.toList.map(clan => QueryStringBindable.bindableString.unbind(clanParam, clan)) ++
          List(value.playerConditionOperator).filter(_ != Or).map(con => QueryStringBindable.bindableString.unbind(operatorParam, con.stringValue))
      }.mkString("&")
    }
  }
  implicit def tagFilterQB: QueryStringBindable[TagFilter] = {
    new QueryStringBindable[TagFilter] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, TagFilter]] = {
        Option(Right(TagFilter(tags = params.get(key).toSet.flatten)))
      }

      override def unbind(key: String, value: TagFilter): String =
        value.tags.toList.map(tag => QueryStringBindable.bindableString.unbind(key, tag)).mkString("&")
    }
  }
  implicit def snFQB: QueryStringBindable[ServerFilter] = {
    new QueryStringBindable[ServerFilter] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ServerFilter]] = {
        params.get(key).map(servers => Right(SimpleServerFilter(servers.toSet))).orElse(Option(Right(NoServerFilter)))
      }

      override def unbind(key: String, value: ServerFilter): String =
      value match {
        case NoServerFilter => ""
        case SimpleServerFilter(servers) =>
          servers.toList.map(server => QueryStringBindable.bindableString.unbind(key, server)).mkString("&")
      }

    }
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


  implicit def focusBinder: QueryStringBindable[Focus] =
    new QueryStringBindable[Focus] {
      val previousField = "previous"
      val nextField = "next"
      val radiusField = "radius"
      override def unbind(key: String, value: Focus): String =
      value match {
        case AsymmetricFocus(p, n) => QueryStringBindable.bindableInt.unbind(previousField, p) + "&" +
          QueryStringBindable.bindableInt.unbind(nextField, n)
        case RadialFocus(r) => QueryStringBindable.bindableInt.unbind(radiusField, r)
        case SimpleFocus => ""
      }
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Focus]] = {
        def bindInt(keyName: String) = QueryStringBindable.bindableInt.bind(key = keyName, params = params)

        PartialFunction.condOpt(List(radiusField, nextField, previousField).map(bindInt)) {
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


  implicit def conditionBindable: PathBindable[TimingCondition] =
    new PathBindable[TimingCondition] {
      override def bind(key: String, value: String): Either[String, TimingCondition] = {
        value match {
          case TimingCondition(timingCondition) => Right(timingCondition)
          case _ => Left(s"Unfamiliar value: $value")
        }
      }

      override def unbind(key: String, value: TimingCondition): String =
        PathBindable.bindableString.unbind(key, value.stringValue)
    }
  implicit def gameTypeBindable: PathBindable[GameType] =
    new PathBindable[GameType] {
      override def bind(key: String, value: String): Either[String, GameType] = {
        value match {
          case GameType(gameType) => Right(gameType)
          case _ => Left(s"Unfamiliar value: $value")
        }
      }
      override def unbind(key: String, value: GameType): String =
        PathBindable.bindableString.unbind(key, value.stringValue)
    }
}
