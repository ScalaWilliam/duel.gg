package us.woop.pinger.persistence

import us.woop.pinger.SauerbratenServerData._
import us.woop.pinger.PingerServiceData.SauerbratenPong

object InputProcessor {

  import PayloadFlatteningImplicits.prepare

  def inputProcessor: PartialFunction[SauerbratenPong, Seq[(String, Seq[Any])]] = {
   import StatementGeneratorImplicits._
    import PayloadFlatteningImplicits._
    {
      case pong@SauerbratenPong(_, _, payload: PlayerCns) =>
        prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: ServerInfoReply) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: ThomasExt) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: TeamScores) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: Uptime) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: HopmodUptime) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: PlayerExtInfo) => prepare(pong, payload)
    }
  }

}
