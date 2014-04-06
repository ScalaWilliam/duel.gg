package us.woop.pinger.persistence

import us.woop.pinger.SauerbratenServerData._
import us.woop.pinger.PingerServiceData.SauerbratenPong
import us.woop.pinger.persistence.CqlInterfacing.AbstractCqlInterface
import us.woop.pinger.SauerbratenServerData.Conversions.ConvertedThomasExt

object InputProcessor {

  /** This turns a sauer pong into a sequence of insert statements and corresponding binds **/
  /** Could be simplified, I'm sure - but needs better understanding of the type system for me **/

  def inputProcessor: PartialFunction[SauerbratenPong, Seq[(String, Seq[Any])]] = {
   import StatementGeneratorImplicits._

    def prepare[T<:Product](pong: SauerbratenPong, payload: T)(implicit hu: AbstractCqlInterface[T]) =
      Seq((hu.makeInsertQuery, hu.stmtValues(pong, payload)))

    {
      case pong@SauerbratenPong(_, _, payload: PlayerCns) =>
        prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: Conversions.ConvertedServerInfoReply) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: Conversions.ConvertedThomasExt) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: Conversions.ConvertedTeamScore) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: Uptime) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: Conversions.ConvertedHopmodUptime) => prepare(pong, payload)
          case pong@SauerbratenPong(_, _, payload: PlayerExtInfo) => prepare(pong, payload)
          case pong@ SauerbratenPong(_, _, _: HopmodUptime) => Seq.empty
          case pong@ SauerbratenPong(_, _, _: ServerInfoReply) => Seq.empty
          case pong@ SauerbratenPong(_, _, _: TeamScores) => Seq.empty
          case pong@ SauerbratenPong(_, _, _: ThomasExt) => Seq.empty
    }
  }

}
