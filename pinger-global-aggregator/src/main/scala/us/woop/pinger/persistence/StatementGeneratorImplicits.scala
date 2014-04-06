package us.woop.pinger.persistence

import us.woop.pinger.SauerbratenServerData.Conversions.{ConvertedHopmodUptime, ConvertedTeamScore, ConvertedThomasExt, ConvertedServerInfoReply}
import us.woop.pinger.SauerbratenServerData.{PlayerExtInfo, Uptime, PlayerCns}
import us.woop.pinger.persistence.CqlInterfacing.AbstractCqlInterface

object StatementGeneratorImplicits {

  implicit object ResolveServerInfoReply extends AbstractCqlInterface[ConvertedServerInfoReply](
    tableName = "simplex.serverinforeply"
  )

  implicit object ResolveThomasExt extends AbstractCqlInterface[ConvertedThomasExt](
    tableName = "simplex.thomasext"
  )

  implicit object ResolveTeamScore extends AbstractCqlInterface[ConvertedTeamScore](
    tableName = "simplex.teamscore"
  )

  implicit object ResolvePlayerCns extends AbstractCqlInterface[PlayerCns](
    tableName = "simplex.playercns"
  )

  implicit object ResolveUptime extends AbstractCqlInterface[Uptime](
    tableName = "simplex.uptime"
  )

  implicit object ResolveHopmodUptime extends AbstractCqlInterface[ConvertedHopmodUptime](
    tableName = "simplex.hopmoduptime"
  )

  implicit object ResolvePlayerExtInfo extends AbstractCqlInterface[PlayerExtInfo](
    tableName = "simplex.playerextinfo"
  )
}
