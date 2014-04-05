package us.woop.pinger.persistence

import us.woop.pinger.SauerbratenServerData._
import us.woop.pinger.SauerbratenServerData.Conversions._
import us.woop.pinger.PingerServiceData.SauerbratenPong
import us.woop.pinger.persistence.PayloadFlattening.Identity
import us.woop.pinger.persistence.PayloadFlattening.Process
import us.woop.pinger.persistence.StatementGeneration.StatementGenerator

object PayloadFlatteningImplicits {

  implicit object a extends Identity[PlayerCns]

  implicit object b extends Process[ServerInfoReply, ConvertedServerInfoReply] {
    override def process(input: ServerInfoReply) =
      Seq(ConvertedServerInfoReply.convert(input))
  }

  implicit object c extends Process[ThomasExt, ConvertedThomasExt] {
    override def process(input: ThomasExt) =
      ConvertedThomasExt.convert(input)
  }

  implicit object d extends Process[TeamScores, ConvertedTeamScore] {
    override def process(input: TeamScores) =
      ConvertedTeamScore.convert(input)
  }

  implicit object e extends Identity[Uptime]

  implicit object f extends Process[HopmodUptime, ConvertedHopmodUptime] {
    override def process(input: HopmodUptime) =
      Seq(ConvertedHopmodUptime.convert(input))
  }

  implicit object g extends Identity[PlayerExtInfo]

  def prepare[V <: Product, U](pong: SauerbratenPong, payload: U)(implicit ev: Process[U, V], evv: StatementGenerator[V]): Seq[(String, List[Any])] = {
    implicitly[Process[U, V]].intoPair(pong, payload)
  }
}
