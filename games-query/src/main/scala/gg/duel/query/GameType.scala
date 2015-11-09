package gg.duel.query

import gg.duel.query.GameType.{ClanCtf, Ctf, Duel}

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
      case Ctf => simpleGame.gameType == "ctf"
      case Duel => simpleGame.gameType == "duel"
      case ClanCtf => simpleGame.tags.contains("clanwar")
    }
}


object GameType {

  case object Ctf extends GameType {
    override val stringValue: String = "ctf"
  }

  case object Duel extends GameType {
    override val stringValue: String = "duel"
  }

  case object ClanCtf extends GameType {
    override val stringValue: String = "clanctf"
  }

  def unapply(string: String): Option[GameType] = PartialFunction.condOpt(string) {
    case Ctf.stringValue => Ctf
    case ClanCtf.stringValue => ClanCtf
    case Duel.stringValue => Duel
  }

  def apply(map: Map[String, Seq[String]]): Either[String, GameType] = {
    map.get("type").flatMap(_.lastOption) match {
      case None => Right(Duel)
      case Some(GameType(gameType)) => Right(gameType)
      case Some(otherType) => Left("Unknown gameType given")
    }
  }

}
