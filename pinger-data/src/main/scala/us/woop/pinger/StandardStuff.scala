package us.woop.pinger

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import us.woop.pinger.data.Stuff.Server

object StandardStuff {
  val configStr =
    """
akka {
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    netty.tcp {
      hostname = "127.0.0.1"
      port = 52552
    }
    secure-cookie = "C802510E1ECC5A7C18AC4DFE489CEAB231D97AAF"
    require-cookie = on
  }
}
    """

  val servers =
    """
      |rb0.butchers.su
      |rb1.butchers.su
      |rb1.butchers.su 20000
      |rb2.butchers.su
      |rb3.butchers.su
      |vaq-clan.de
      |effic.me 10000
      |effic.me 20000
      |effic.me 30000
      |effic.me 40000
      |effic.me 50000
      |effic.me 60000
      |psl.sauerleague.org 20000
      |psl.sauerleague.org 30000
      |psl.sauerleague.org 40000
      |psl.sauerleague.org 50000
      |psl.sauerleague.org 60000
      |sauer.woop.us
      |vaq-clan.de
      |butchers.su
      |noviteam.de
      |darkkeepers.dk 28786
    """.stripMargin

  val noPort = """^([^ ]+)$""".r
  val withPort = """^([^ ]+) ([0-9]+)$""".r
  val serversParsed = servers.split("\n").map(_.trim).filter(_.nonEmpty).collect {
    case withPort(host, port) => Server(host, port.toInt)
    case noPort(host) => Server(host)
  }
  val configData = ConfigFactory.systemProperties().withFallback(ConfigFactory.parseString(configStr).withFallback(ConfigFactory.load()))

  implicit lazy val actorSystem = ActorSystem("PingerService", configData)


  val dataName = {
    val df = new SimpleDateFormat("dd-HH")
    df.format(new Date())
  }

  val levelDbTarget = new File(new File("indexed-data"), dataName)
}
