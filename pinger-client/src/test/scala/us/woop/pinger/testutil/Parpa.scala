package us.woop.pinger.testutil

import akka.actor._
import us.woop.pinger.{MasterserverClient, PingerClient}
import us.woop.pinger.PingerClient.Ready

/** 31/01/14 */
object Parpa extends App {

  val as = ActorSystem("heloo")

  import akka.actor.ActorDSL._


  val receiver = actor(as)(new Act {

    import PingerClient._

    become {
      case Ready =>
        println("Ready!")

        import PingerClient._
        println("Ready once.")
        //val sservers = ("81.169.137.114", 20000) :: Nil
        //val sservers = ("85.10.195.182",30000)::Nil
        //val sservers = ("146.185.167.115", 28785) :: Nil
        val sservers = ("146.185.167.115", 28785) ::("127.0.0.1", 1234) :: Nil
        for {
          (h, p) <- sservers.take(100)
        } sender ! Ping(h, p)

      case x => println(x)
    }
  })


  //val f = ({ case GetServerInfoReply(r) => r }).li


  // List((109.73.51.58,10050), (109.73.51.58,28785), (109.73.51.58,10044), (134.0.24.218,28785), (108.174.48.250,80), (89.35.181.113,35000), (173.255.193.121,28785), (89.35.181.113,28785), (5.175.166.93,28785), (172.245.41.76,28785), (88.198.108.163,28785), (85.214.214.91,28785), (31.31.203.105,28785), (192.81.216.48,20000), (95.85.28.218,10000), (95.85.28.218,30000), (95.85.28.218,60000), (95.85.28.218,40000), (95.85.28.218,50000), (95.85.28.218,20000), (188.40.118.114,28785), (188.40.118.113,28785), (85.214.29.156,10000), (85.214.29.156,20000), (85.214.29.156,40000), (144.76.157.141,10000), (176.9.75.98,10070), (109.73.51.58,10010), (109.73.51.58,10030), (96.231.199.244,28785), (81.169.137.114,10000), (81.169.137.114,20000), (81.169.137.114,30000), (87.98.139.152,28785), (83.169.44.106,14000), (96.231.199.244,28787), (85.214.29.156,30000), (85.214.214.91,28900), (195.34.136.67,20000), (91.211.244.91,28785), (91.211.244.91,28795), (91.211.244.91,20000), (109.74.195.195,28899), (109.74.195.195,28785), (85.214.66.181,6666), (85.214.66.181,10000), (120.138.18.82,28785), (95.156.230.73,32370), (78.46.73.228,30000), (78.46.73.228,10000), (78.46.73.228,20000), (78.46.73.228,40000), (78.46.73.228,50000), (74.91.115.200,28785), (178.4.3.99,28785), (188.164.130.6,4999), (85.114.142.164,20000), (79.110.128.93,28785), (79.110.128.93,20000), (31.220.51.27,24987), (37.187.5.145,50000), (37.187.5.145,20000), (37.187.5.145,10000), (37.187.5.145,40000), (37.187.5.145,30000), (188.164.130.6,1111), (176.28.47.108,28785), (134.255.220.143,28785), (88.191.246.101,28785), (88.191.246.101,10000), (88.191.246.101,20010), (88.191.246.101,20020), (155.185.232.162,28785), (188.164.130.6,34234), (188.164.130.6,25010), (69.195.137.18,28789), (69.195.137.18,28785), (178.16.34.167,28785), (62.112.211.12,28785), (23.94.28.21,50010), (23.94.28.21,50030), (23.94.28.21,50000), (78.47.184.3,1337), (178.63.3.34,4999), (178.63.3.34,3999), (178.63.3.34,1999), (178.63.3.34,1111), (217.115.159.248,28785), (188.164.130.6,3335), (188.164.130.6,25000), (188.164.130.6,12345), (78.129.230.93,28785), (83.117.66.133,28785), (188.164.130.6,25040), (188.164.130.6,25030), (188.164.130.6,4446), (91.211.244.91,4060), (91.211.244.91,4070), (91.211.244.91,4020), (192.81.216.48,30000), (50.21.129.159,28785), (192.81.216.48,10000), (31.220.48.22,38785), (178.238.226.172,28785), (192.95.30.66,28785), (192.81.216.48,40000), (62.75.213.175,1234), (69.136.162.167,25000), (91.211.244.91,4010), (91.211.244.91,4050), (91.211.244.91,4040), (91.211.244.91,4030), (91.211.244.91,4000), (23.94.28.21,50020), (80.117.69.202,54345), (192.81.216.48,50000), (78.47.23.219,28785), (62.75.213.175,10000), (138.91.116.177,28785), (62.75.213.175,28785), (62.75.213.175,1337), (178.32.95.181,28785), (184.22.86.114,28785), (146.185.167.115,28785), (5.135.216.194,28785), (5.135.216.194,11100), (5.135.216.194,28796), (188.164.130.6,50000), (188.164.130.6,50010), (188.164.130.6,50020), (188.164.130.6,50030), (5.231.58.11,28785), (5.231.58.11,2000), (62.75.213.175,20000), (188.164.130.6,25020), (188.164.130.6,3999), (188.164.130.6,29785), (188.164.130.6,28785), (75.129.117.137,28785), (188.164.130.6,22224), (84.149.240.180,28785), (188.164.130.6,35234), (185.38.46.194,60010), (185.38.46.194,60000), (188.164.130.6,1999), (185.38.46.194,61234), (93.209.15.202,10020), (188.164.130.6,26030), (188.164.130.6,26010), (188.164.130.6,26060), (188.164.130.6,26040), (188.164.130.6,26020), (188.164.130.6,26000), (188.164.130.6,26050), (188.164.130.6,26070), (24.138.248.86,28785), (85.10.195.182,11011), (85.10.195.182,28785), (85.10.195.182,30000), (85.10.195.182,50000), (79.93.160.11,28785), (69.136.162.167,35000), (68.150.161.181,20000), (95.85.48.85,28785), (188.164.130.6,22222))

  val pa = as.actorOf(Props(classOf[PingerClient], receiver))


  lazy val servers = MasterserverClient.getServers(sauerMasterserver)

  val sauerMasterserver = ("sauerbraten.org", 28787)
  //  println(servers)


}
