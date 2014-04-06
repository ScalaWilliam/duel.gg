package us.woop.pinger.persistence

import us.woop.pinger.SauerbratenServerData._
import us.woop.pinger.SauerbratenServerData.Conversions._
import us.woop.pinger.PingerServiceData.SauerbratenPong
import us.woop.pinger.persistence.PayloadFlattening.{ProcessToDifferent, ProcessToMany, Identity, Process}
import us.woop.pinger.persistence.CqlInterfacing.AbstractCqlInterface

/** These are the implicits that flatten objects if they have nested contents. **/
object PayloadFlatteningImplicits {

  implicit object FlattenUptime extends Identity[Uptime]

  implicit object PlayerCnsFlatten extends Identity[PlayerCns]

  implicit object FlattenPlayerExtInfo extends Identity[PlayerExtInfo]

  implicit object FlattenHopmodUptime extends ProcessToDifferent(ConvertedHopmodUptime.convert)

  implicit object FlattenServerInfoReply extends ProcessToDifferent(ConvertedServerInfoReply.convert)

  implicit object FlattenThomasExt extends ProcessToMany(ConvertedThomasExt.convert)

  implicit object FlattenTeamScore extends ProcessToMany(ConvertedTeamScore.convert)

  /** Convert a pong (with the same pong as is inside) into a Seq of (Sql Query --> Bind parameters) **/
  def prepare[V <: Product, U](pong: SauerbratenPong, payload: U)(implicit ev: Process[U, V], evv: AbstractCqlInterface[V]): Seq[(String, List[Any])] = {
    implicitly[Process[U, V]].intoPair(pong, payload)
  }

}
