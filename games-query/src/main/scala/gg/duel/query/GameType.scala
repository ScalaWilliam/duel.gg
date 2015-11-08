package gg.duel.query

import gg.duel.query.GameType.{All, Ctf, Duel}
import gg.duel.query.QueryableGame$

/**
 * Created on 30/10/2015.
 */
sealed trait GameType extends (QueryableGame => Boolean) {
  def stringValue: String
  def toMap: Map[String, Seq[String]] = {
    this match {
      case All => Map.empty
      case other => Map("type" -> List(stringValue))
    }
  }
  override def apply(simpleGame: QueryableGame): Boolean =
    this match {
      case All => true
      case Ctf => simpleGame.gameType == "ctf"
      case Duel => simpleGame.gameType == "duel"
    }
}


object GameType {

  case object Ctf extends GameType {
    override val stringValue: String = "ctf"
  }

  case object All extends GameType {
    override val stringValue: String = "all"
  }

  case object Duel extends GameType {
    override val stringValue: String = "duel"
  }

  def unapply(string: String): Option[GameType] = PartialFunction.condOpt(string) {
    case Ctf.stringValue => Ctf
    case All.stringValue => All
    case Duel.stringValue => Duel
  }

  def apply(map: Map[String, Seq[String]]): Either[String, GameType] = {
    map.get("type").flatMap(_.lastOption) match {
      case None => Right(All)
      case Some(GameType(gameType)) => Right(gameType)
      case Some(otherType) => Left("Unknown gameType given")
    }
  }

}
