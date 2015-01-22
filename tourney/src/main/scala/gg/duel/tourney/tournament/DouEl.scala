package gg.duel.tourney.tournament

import scala.xml.PrettyPrinter


object DouEl extends App {

//  val edob = new EightPlayerDoubleEliminationTournament(('A' to 'H').map(_.toString).toList)
  val edob = new SixteenPlayerDoubleEliminationTournament(('A' to 'H').map(_.toString).toList ++ ('A' to 'H').map(_.toString).map("2"+_).toList)
  edob.games(1).winner = "A"
  edob.games(2).winner = "D"
  edob.games(3).winner = "E"
  edob.games(5).fail("not showed up")
  def pretty(input: scala.xml.Elem) = new PrettyPrinter(160,2).format(input)
  def prettyPrint(input: scala.xml.Elem) = println(pretty(input))
  val projectedTournament = edob.toXml
  val reloadedTournament = Tournament.loadFromXml(projectedTournament)
  val reprojectedTournament = reloadedTournament.toXml
  prettyPrint(reprojectedTournament)
//  println(projectedTournament == reprojectedTournament)

}