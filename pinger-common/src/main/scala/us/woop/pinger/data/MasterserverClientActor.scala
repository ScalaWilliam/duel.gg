package us.woop.pinger.data

object MasterserverClientActor {

   case object RefreshServerList

   case class MasterServers(servers: Set[(String, Int)])

   case class ServerGone(server: (String, Int))

   case class ServerAdded(server: (String, Int))

 }
