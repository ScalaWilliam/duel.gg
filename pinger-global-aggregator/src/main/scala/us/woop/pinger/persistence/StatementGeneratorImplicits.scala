package us.woop.pinger.persistence

import us.woop.pinger.SauerbratenServerData.Conversions.{ConvertedHopmodUptime, ConvertedTeamScore, ConvertedThomasExt, ConvertedServerInfoReply}
import us.woop.pinger.SauerbratenServerData.{PlayerExtInfo, Uptime, PlayerCns}
import us.woop.pinger.persistence.StatementGeneration.StatementGenerator

object StatementGeneratorImplicits {

  implicit object ResolveServerInfoReply extends StatementGenerator[ConvertedServerInfoReply](
    tableName = "simplex.serverinforeply"
  )

  implicit object ResolveThomasExt extends StatementGenerator[ConvertedThomasExt](
    tableName = "simplex.thomasext"
  )

  implicit object ResolveTeamScore extends StatementGenerator[ConvertedTeamScore](
    tableName = "simplex.teamscore"
  )

  implicit object ResolvePlayerCns extends StatementGenerator[PlayerCns](
    tableName = "simplex.playercns"
  )

  implicit object ResolveUptime extends StatementGenerator[Uptime](
    tableName = "simplex.uptime"
  )

  implicit object ResolveHopmodUptime extends StatementGenerator[ConvertedHopmodUptime](
    tableName = "simplex.hopmoduptime"
  )

  implicit object ResolvePlayerExtInfo extends StatementGenerator[PlayerExtInfo](
    tableName = "simplex.playerextinfo"
  )
}
