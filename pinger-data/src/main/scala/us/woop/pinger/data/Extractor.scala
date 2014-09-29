package us.woop.pinger.data

import akka.util.ByteString
import PongParser._
import us.woop.pinger.data.ParsedPongs.Conversions.{ConvertHopmodUptime, ConvertServerInfoReply, ConvertTeamScore, ConvertThomasExt}

object Extractor {

  def extract: PartialFunction[ByteString, Seq[Any]] = {

    {
      case GetRelaxedPlayerExtInfo(x) => Seq(x)
      case GetServerInfoReply(x) => Seq(x) ++ Seq(ConvertServerInfoReply.convert(x))
      case GetPlayerCns(x) => Seq(x)
      case GetHopmodUptime(x) => Seq(x) ++ Seq(ConvertHopmodUptime.convert(x))
      case GetTeamScores(x) => Seq(x) ++ ConvertTeamScore.convert(x)
      case GetUptime(x) => Seq(x)
      case GetThomasModExtInfo(x) => Seq(x)
      case CheckOlderClient(x) => Seq(x)
    }
  }

}