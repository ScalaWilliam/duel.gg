package gg.duel.pinger.masterserver

import org.scalatest.{Matchers, WordSpec}

class MasterserverClientTest extends WordSpec with Matchers {

  "MasterserverClient" should {
    "list some servers" in {
      MasterserverClient.default.getServers should (
        contain ("46.101.249.112" -> 20000)
        or contain ("195.154.128.64" -> 60000)
        )
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
