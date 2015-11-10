package gg.duel.query

import gg.duel.query.GameType.{All, ClanCtf, CtfOnly, Duel}

/**
 * Created on 30/10/2015.
 */
sealed trait GameType extends (QueryableGame => Boolean) {
  def stringValue: String
  def toMap: Map[String, Seq[String]] = {
    this match {
      case Duel => Map.empty
      case other => Map("type" -> List(stringValue))
    }
  }
  override def apply(simpleGame: QueryableGame): Boolean =
    this match {
      case All => true
      case CtfOnly => simpleGame.gameType == "ctf"
      case Duel => simpleGame.gameType == "duel"
      case ClanCtf => simpleGame.tags.contains("clanwar")
    }
}


object GameType {

  sealed trait Ctf extends GameType

  case object CtfOnly extends Ctf {
    override val stringValue: String = "ctf"
  }

  case object Duel extends GameType {
    override val stringValue: String = "duel"
  }

  case object All extends GameType {
    override val stringValue: String = "all"
  }

  case object ClanCtf extends Ctf {
    override val stringValue: String = "clanctf"
  }

  def unapply(string: String): Option[GameType] = PartialFunction.condOpt(string) {
    case CtfOnly.stringValue => CtfOnly
    case ClanCtf.stringValue => ClanCtf
    case Duel.stringValue => Duel
    case All.stringValue => All
  }

  def apply(map: Map[String, Seq[String]]): Either[String, GameType] = {
    map.get("type").flatMap(_.lastOption) match {
      case None => Right(All)
      case Some(GameType(gameType)) => Right(gameType)
      case Some(otherType) => Left("Unknown gameType given")
    }
  }

}
