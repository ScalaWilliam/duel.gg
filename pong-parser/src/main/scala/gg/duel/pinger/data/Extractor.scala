package gg.duel.pinger.data

import akka.util.ByteString
import gg.duel.pinger.data.ParsedPongs.Conversions.{ConvertHopmodUptime, ConvertServerInfoReply, ConvertTeamScore}
import gg.duel.pinger.data.PongParser._

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

  def extractDuel: PartialFunction[ByteString, Seq[Any]] = {

    {
      case GetServerInfoReply(x) => Seq(x) ++ Seq(ConvertServerInfoReply.convert(x))
      case GetRelaxedPlayerExtInfo(x) => Seq(x)
      case _ => Seq.empty
    }
  }

}
