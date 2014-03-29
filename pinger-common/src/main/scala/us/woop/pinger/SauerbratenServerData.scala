package us.woop.pinger

object SauerbratenServerData {

  case class ServerInfoReply(clients: Int, protocol: Int, gamemode: Int, remain: Int, maxclients: Int,
                             gamepaused: Option[Int], gamespeed: Option[Int], mapname: String, desc: String)


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

}
