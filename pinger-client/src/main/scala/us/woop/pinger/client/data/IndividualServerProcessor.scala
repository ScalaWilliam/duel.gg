package us.woop.pinger.client.data

object IndividualServerProcessor {
  sealed trait ServerState
  case class Online(gameStatus: GameStatus) extends ServerState
  case object Offline extends ServerState
  case object Initialising extends ServerState

  sealed trait GameStatus
  case object Empty extends GameStatus
  case object Active extends GameStatus

  case object Ping
  case object Refresh

  case object GetState
}
