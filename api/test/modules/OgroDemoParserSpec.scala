package modules

import org.scalatest.WordSpec
import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

import org.scalatestplus.play.{PlaySpec, OneAppPerSuite}
import play.api.libs.ws.{WS, WSClient}

class OgroDemoParserSpec extends PlaySpec with OneAppPerSuite {
  "ogro demo parser" must {
    "Download and parse correctly" in {
      implicit val sslClient = WS.client
      import scala.concurrent.ExecutionContext.Implicits.global
      val odp = new OgroDemoParser()
      val res = await(odp.getDemosF("ogros.org 1"))
      res mustNot be(empty)

      info(s"First result returned: ${res.headOption}")
    }
  }
}
