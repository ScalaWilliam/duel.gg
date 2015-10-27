package gg.duel.pinger.masterserver

import org.scalatest.{Matchers, WordSpec}

class MasterserverClientTest extends WordSpec with Matchers {

  "MasterserverClient" should {
    "list some servers" in {
      MasterserverClient.default.getServers should contain ("92.222.216.113" -> 10000)
    }
  }
  "Parser" should {
    "list one server" in {
      MasterserverClient.Parser(
        lines = List("addserver 123.41.13.24 1234", "addserver 123.41.13.24 12345")
      ).getServers should contain only("123.41.13.24" -> 1234, "123.41.13.24" -> 12345)
    }
  }

}
