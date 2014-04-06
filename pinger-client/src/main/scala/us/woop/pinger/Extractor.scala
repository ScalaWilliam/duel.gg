package us.woop.pinger

import us.woop.pinger.SauerbratenProtocol._


object Extractor {

  def extract: PartialFunction[List[_], Seq[Any]] = {

    import FlatteningImplicits._

    {
      case GetRelaxedPlayerExtInfo(x) => Seq(x)
      case GetServerInfoReply(x) => Seq(x) ++ prepare(x)
      case GetPlayerCns(x) => Seq(x)
      case GetHopmodUptime(x) => Seq(x) ++ prepare(x)
      case GetTeamScores(x) => Seq(x) ++ prepare(x)
      case GetUptime(x) => Seq(x)
      case GetThomasModExtInfo(x) => Seq(x) ++ prepare(x)
      case CheckOlderClient(x) => Seq(x)
    }
  }

}