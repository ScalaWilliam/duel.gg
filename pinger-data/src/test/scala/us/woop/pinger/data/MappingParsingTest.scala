package us.woop.pinger.data

import org.scalatest.{Inspectors, WordSpecLike, Matchers}
import org.scalatest.matchers.{Matcher, MatchResult}
import akka.util.ByteString
import scala.util.Try
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import ParsedPongs._
import ParsedPongs.Uptime
import ParsedPongs.PlayerCns
import ParsedPongs.ServerInfoReply
import ParsedPongs.PlayerExtInfo
import ParsedPongs.HopmodUptime
import us.woop.pinger.PongParser

@RunWith(classOf[JUnitRunner])
class MappingParsingTest extends WordSpecLike with Matchers with Inspectors {

  def parseAs(expected: Any) = new Matcher[ByteString] {
    override def apply(left: ByteString): MatchResult = {
      val output = Try(PongParser.matchers apply left)
      MatchResult(
        output.isSuccess && output.get == expected,
        s"""Failed to transform data into $expected - $output""",
        s"""Successfully transformed data into $expected - $output"""
      )
    }
  }

  "the matcher" must {
    "parse a standard info reply" in {
      ByteString(1, 1, 1, 5, 5, -128, 3, 1, 3, -128, -61, 1, 17,
        1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0
      ) should parseAs(ServerInfoReply(5, 259, 3, 451, 17, None, None, "frozen", "sauer.woop.us"))
    }
    "parse a hopmod uptime reply" in {
      ByteString(0, 0, -1, -1, 105, -127, -31, -121, 3, 0, -2, 1, -1, 68,
        101, 99, 32, 49, 53, 32, 50, 48, 49, 51, 32, 49, 56, 58, 51, 53, 58, 51, 55, 0
      ) should parseAs(HopmodUptime(Uptime(105, 231393), 1, -1, "Dec 15 2013 18:35:37"))
    }
    "parse the list of player cns" in {
      ByteString(0, 1, -1, -1, 105, 0, -10, 0, 1, 4, 5, 2
      ) should parseAs(PlayerCns(105, List(0, 1, 4, 5, 2)))
    }
    "parse w00p|Dr.Akkå extinfo with non-standard characters" in {
      ByteString(0, 1, -1, -1, 105, 0, -11, 0, 33, 119, 48, 48, 112,
        124, 68, 114, 46, 65, 107, 107, -121, 0, 103, 111, 111, 100, 0, 0, 0, 0, 0, 0, 100, 0, 6, 3, 5, 91, 121, -74
      ) should parseAs(PlayerExtInfo(105, 0, 33, "w00p|Dr.Akkå", "good", 0, 0, 0, 0, 100, 0, 6, 3, 5, "91.121.182.x"))
    }
    "parse another player" in {
      ByteString(0, 1, -1, -1, 105, 0, -11, 1, 42, 107, 105, 110, 103,
        0, 103, 111, 111, 100, 0, 11, 0, 11, 0, 22, 1, 0, 4, 0, 0, 5, 15, -101
      ) should parseAs(PlayerExtInfo(105, 1, 42, "king", "good", 11, 11, 0, 22, 1, 0, 4, 0, 0, "5.15.155.x"))
    }
    "parse Uez" in {
      ByteString(0, 1, -1, -1, 105, 0, -11, 4, 49, 85, 101, 122, 0,
        103, 111, 111, 100, 0, 9, 0, 14, 0, 20, -99, 0, 4, 0, 1, 109, 29, -77
      ) should parseAs(PlayerExtInfo(105, 4, 49, "Uez", "good", 9, 14, 0, 20, -99, 0, 4, 0, 1, "109.29.179.x"))
    }
    "parse GiftzZ" in {
      ByteString(0, 1, -1, -1, 105, 0, -11, 5, 30, 71, 105, 102, 116,
        122, 90, 0, 103, 111, 111, 100, 0, 14, 0, 11, 0, 28, 1, 0, 4, 0, 0, 109, -64, -8
      ) should parseAs(PlayerExtInfo(105, 5, 30, "GiftzZ", "good", 14, 11, 0, 28, 1, 0, 4, 0, 0, "109.192.248.x"))
    }
    "parse Nexus" in {
      ByteString(0, 1, -1, -1, 105, 0, -11, 2, 84, 78, 101, 120, 117,
        115, 0, 103, 111, 111, 100, 0, 18, 0, 16, 0, 33, 1, 0, 4, 0, 0, 46, 78, 86
      ) should parseAs(PlayerExtInfo(105, 2, 84, "Nexus", "good", 18, 16, 0, 33, 1, 0, 4, 0, 0, "46.78.86.x"))
    }
    "Parse team scores" in {
      ByteString(0, 2, -1, -1, 105, 1, 3, -128, -61, 1
      ) should parseAs(TeamScores(105, 3, 451, List()))
    }
  }
}
