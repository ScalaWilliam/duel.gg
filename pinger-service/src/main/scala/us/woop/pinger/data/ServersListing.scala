package us.woop.pinger.data

import scala.util.Try

object ServersListing {

  val servers = """
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
                  |darkkeepers.dk:28786
                  |""".stripMargin.split("\r?\n").filterNot(_.isEmpty).toVector.flatMap(h => Try(Server.fromAddress(h)).toOption.toVector)

}
