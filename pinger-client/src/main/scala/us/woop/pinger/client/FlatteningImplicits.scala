package us.woop.pinger.client

import us.woop.pinger.SauerbratenServerData.Conversions.{ConvertedTeamScore, ConvertedThomasExt, ConvertedServerInfoReply, ConvertedHopmodUptime}
import FlatteningAbstracts.{ProcessToDifferent, ProcessToMany, Process}

object FlatteningImplicits {

  implicit object FlattenHopmodUptime extends ProcessToDifferent(ConvertedHopmodUptime.convert)

  implicit object FlattenServerInfoReply extends ProcessToDifferent(ConvertedServerInfoReply.convert)

  implicit object FlattenThomasExt extends ProcessToMany(ConvertedThomasExt.convert)

  implicit object FlattenTeamScore extends ProcessToMany(ConvertedTeamScore.convert)

  /** Convert a pong (with the same pong as is inside) into a Seq of (Sql Query --> Bind parameters) **/
  def prepare[V, U](message: U)(implicit ev: Process[U, V]): Seq[Any] = {
    implicitly[Process[U, V]].process(message)
  }

}
