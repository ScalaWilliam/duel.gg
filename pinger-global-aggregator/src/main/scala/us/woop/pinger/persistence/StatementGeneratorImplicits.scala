package us.woop.pinger.persistence

import us.woop.pinger.SauerbratenServerData.Conversions.{ConvertedHopmodUptime, ConvertedTeamScore, ConvertedThomasExt, ConvertedServerInfoReply}
import us.woop.pinger.SauerbratenServerData.{PlayerExtInfo, Uptime, PlayerCns}
import us.woop.pinger.persistence.StatementGeneration.StatementGenerator

object StatementGeneratorImplicits {

  implicit object ResolveServerInfoReply extends StatementGenerator[ConvertedServerInfoReply] {
    override val tableName = "simplex.serverinforeply"
  }

  implicit object ResolveThomasExt extends StatementGenerator[ConvertedThomasExt] {
    override val tableName = "simplex.thomasext"
  }

  implicit object ResolveTeamScore extends StatementGenerator[ConvertedTeamScore] {
    override val tableName = "simplex.teamscore"
  }

  implicit object ResolvePlayerCns extends StatementGenerator[PlayerCns] {
    override val tableName = "simplex.playercns"
  }

  implicit object ResolveUptime extends StatementGenerator[Uptime] {
    override val tableName = "simplex.uptime"
  }

  implicit object ResolveHopmodUptime extends StatementGenerator[ConvertedHopmodUptime] {
    override val tableName = "simplex.hopmoduptime"
  }

  implicit object ResolvePlayerExtInfo extends StatementGenerator[PlayerExtInfo] {
    override val tableName = "simplex.playerextinfo"
  }
}
