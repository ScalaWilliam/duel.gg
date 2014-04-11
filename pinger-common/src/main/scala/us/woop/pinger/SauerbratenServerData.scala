package us.woop.pinger

object SauerbratenServerData {

  case class ServerInfoReply(clients: Int, protocol: Int, gamemode: Int, remain: Int, maxclients: Int,
                             gamepaused: Option[Int], gamespeed: Option[Int], mapname: String, description: String)

  case class Uptime(version: Int, totalsecs: Int)

  case class HopmodUptime(uptime: Uptime, hopmodVersion: Int, hopmodRevision: Int, buildTime: String)

  case class PlayerCns(version: Int, cns: List[Int])
  case class PlayerExtInfo(version: Int, cn: Int, ping: Int, name: String, team: String, frags: Int,
                           deaths: Int, teamkills: Int, accuracy: Int, health: Int, armour: Int, gun: Int, privilege: Int, state: Int, ip: String)

  case class ThomasExt(ds: List[ThomasD], r: ThomasR)
  case class ThomasD(data: List[Int])
  case class ThomasR(s1: Option[String], s2: String, data: List[Int])
  case class OlderClient()
  case class TeamScores(version: Int, gamemode: Int, remain: Int, scores: List[TeamScore])

  case class TeamScore(name: String, score: Int, baseMap: Boolean, baseScores: List[Int])


  object Conversions {

    case class ConvertedHopmodUptime(version: Int, totalsecs: Int, hopmodVersion: Int, hopmodRevision: Int, buildTime: String)
    object ConvertedHopmodUptime {
      def convert(obj: HopmodUptime): ConvertedHopmodUptime = {
        import obj._
        import uptime._
        ConvertedHopmodUptime(
          version = version, totalsecs = totalsecs,
          hopmodVersion = hopmodVersion,
          hopmodRevision = hopmodRevision, buildTime = buildTime
        )
      }
    }
//    object Gamemodes {
//      sealed trait Gamemode {
//        def name: String
//        def num: Int
//        val isTeam = false
//        val hasPickups = false
//        val isInsta = false
//        val isEfficiency = false
//        val isCtf = false
//        val isProtect = false
//        val isCapture = false
//        val isHold = false
//      }
//      trait WithTeam {this:Gamemode=> override val isTeam = true }
//      trait HasInsta {this:Gamemode=> override val isInsta = true }
//      trait HasCtf {this:Gamemode=> override val isCtf = true }
//      trait HasPickups {this:Gamemode=> override val hasPickups = true }
//      trait HasCapture {this:Gamemode=> override val isCapture = true }
//      case object Ffa extends Gamemode with HasPickups {
//        val name = "ffa"
//        val num = 0
//      }
//      case object Teamplay extends Gamemode with HasPickups with WithTeam {
//        val name="teamplay"
//        val num = 2
//      }
//      case object Instagib extends Gamemode with HasInsta {
//        val name = "instagib"
//        val num = 3
//      }
//      case object InstagibTeam extends Gamemode with HasInsta with WithTeam {
//        val name = "instagibteam"
//        val num = 4
//      }
//      trait HasEfficiency{this:Gamemode => override val isEfficiency = true}
//      trait HasTactics
//      case object Tactics extends Gamemode with HasTactics {
//        val name = "tactics"
//        val num = 7
//      }
//      case object TacticsTeam extends Gamemode with HasTactics with WithTeam {
//        val name="tacticsteam"
//        val num = 8
//      }
//      case object Capture extends Gamemode with HasCapture with HasPickups with WithTeam {
//        val name = "capture"
//        val num = 9
//      }
//      case object RegenCapture extends Gamemode with HasCapture with WithTeam {
//        val name = "regen"
//        val num = 10
//      }
//      case object Ctf extends Gamemode with WithTeam with HasPickups with HasCtf {
//        val name = "ctf"
//        val num = 11
//      }
//      trait WithProtect { this:Gamemode => override val isProtect = true }
//      case object Protect extends Gamemode with WithTeam with WithProtect with HasPickups {
//        val name = "protect"
//        val num = 11
//      }
//      case object InstaProtect extends Gamemode with WithTeam with WithProtect with HasInsta {
//        val name = "instaprotect"
//        val num = 12
//      }
//      trait WithHold { this: Gamemode => override val isHold = true }
//      case object Hold extends Gamemode with WithTeam with WithHold {
//        val name = "hold"
//        val num = 13
//      }
//      case object InstaHold extends Gamemode with WithTeam with WithHold with HasPickups {
//        val name = "instahold"
//        val num = 14
//      }
//
//    }
//
//    case class Gamemode(name: Gamemodes.Gamemode)
//    object Gamemode {
//
//      val modes = Map(0 -> "ffa", 1 -> "coop", 2 -> "teamplay", 3 -> "insta", 4 -> "instateam")
//
//      def apply(num: Int) = Gamemode{
//
//        0: "ffa" / "default" mode. This is the default normal ffa game, and can also be used as "prewar" while setting up teams / voting for the next game.
//        1: coop edit mode. This simply enables map editing in multiplayer, otherwise identical to mode 0.
//        2: a standard teamplay game. will work with any number of teams with any number of players: you are allied with all players whose "team" setting is the same as yours.
//        3/4: instagib [team] mode. No items will spawn, but everyone will have 100 rifle rounds and 1 health.
//        5/6: efficiency [team] mode. No items will spawn, but everyone will get all weapons with full ammo, and green armour.
//        7/8: tactics [team] mode. No items will spawn, but everyone will spawn with only base ammo for 2 random weapons and green armour.
//        9: capture mode, see capture mode section below.
//        10: regen capture mode, like capture mode but with no respawn timer, and you regenerate health, armour, and ammo by standing on bases you own.
//        11: ctf mode. Capture the flag where you must retrieve the enemy flag and return it to your own flag for points.
//        12: insta ctf mode. Capture the flag as above, but with weapons, health, and items as in instagib mode.
//        13: protect mode. Touch the enemy flag for points. Protect your own flag by picking it up.
//        14: insta protect mode. Like protect mode above, but with weapons, health, and items as in instagib mode.
//        15: hold mode. Hold the flag for a time to score points.
//        16: insta hold mode. Like hold mode above, but with weapons, health, and items as in instagib mode.
//        17: efficiency ctf mode. Capture the flag as above, but with weapons, health, and items as in efficiency mode.
//        18: efficiency protect mode. Like protect mode above, but with weapons, health, and items as in efficiency mode.
//        19: efficiency hold mode. Like hold mode above, but with weapons, health, and items as in efficiency mode.
//        20: collect mode. Frag the enemy team to drop skulls. Collect them and bring them to the enemy base for points.
//        21: insta collect mode. Like collect mode above, but with weapons, health, and items as in instagib mode.
//        22: efficiency collect mode. Like collect mode above, but with weapons, health, and items as in efficiency mode.
//        -1: demo playback mode, see demo recording section below.
//          -2, -3: single player, see single player mode section below.
//
//      }
//    }
//    val gamemodeList = Map(
//      0 -> "ffa", 1 -> "coop", 2 ->"teamplay", 3 -> "insta", 4 -> "instateam", 5 -> "effic", 6 -> "efficteam",
//      7->"tactics", 8->"tacticsteam", 9 -> "capture", 10 -> "rege", 11 -> "ctf", 12 -> "instactf",
//      13 -> "protect", 14 -> "instaprotect", 15 -> "hold", 16 -> "instahold", 17 -> "effictf",
//      18 -> "efficprotect", 19 -> "effichold", 20 -> "collect", 21 -> "instacollect", 22 -> "efficcollect"
//    )
    case class Gamemode(id: Int)
    val validGamemodeIs = (0 to 22).toList
    case class ConvertedServerInfoReply(clients: Int, protocol: Int, gamemode: Option[Gamemode], remain: Int, maxclients: Int,
                                        gamepaused: Boolean, gamespeed: Int, mapname: String, description: String)

    object ConvertedServerInfoReply {
      def convert (obj: ServerInfoReply): ConvertedServerInfoReply = {
        import obj._
        ConvertedServerInfoReply(
          clients = clients, protocol = protocol, gamemode = validGamemodeIs.lift.apply(gamemode).map(Gamemode),
          remain = remain, maxclients = maxclients, gamepaused = gamepaused.getOrElse(0) != 0,
          gamespeed = gamespeed.getOrElse(100), mapname = mapname, description = description
        )
      }
    }

    case class ConvertedThomasExt(i: Int, dataA: List[Int], dataB: List[Int], s1: String, s2: String)

    object ConvertedThomasExt {
      def convert(ext: ThomasExt): Seq[ConvertedThomasExt] = {
        val converted: Seq[ConvertedThomasExt] = for {
          ThomasExt(thomasDs, ThomasR(s1, s2, dataB)) <- ext :: Nil
          ThomasD(dataA) <- thomasDs
        } yield ConvertedThomasExt(0, dataA, dataB, s1.getOrElse(""), s2)
        converted.zipWithIndex.map{case(e, i) => e.copy(i = i)}
      }
    }

    case class ConvertedTeamScore(scoreNum: Int, version: Int, gamemode: Int, remain: Int, name: String, score: Int, baseMap: Boolean, baseScores: List[Int])

    object ConvertedTeamScore {
      def convert(ts: TeamScores): Seq[ConvertedTeamScore] = {
        val converted: Seq[ConvertedTeamScore] = for {
          TeamScores(version, gamemode, remain, scores) <- ts :: Nil
          TeamScore(name, score, baseMap, baseScores) <- scores
        } yield ConvertedTeamScore(0, version, gamemode, remain, name, score, baseMap, baseScores)
        converted.zipWithIndex.map{case(e,i)=>e.copy(scoreNum = i)}
      }
    }

  }

}
