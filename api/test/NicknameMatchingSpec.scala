import modules.NicknameMatcher
import org.scalatest.{Matchers, WordSpec}

class NicknameMatchingSpec extends WordSpec with Matchers {
  "Nickname Matcher" must {
    "Match correctly" in {
      NicknameMatcher("w00p|*")("w00p|Drakas") shouldBe true
      NicknameMatcher("w00p|*")("xw00p|Drakas") shouldBe false
      NicknameMatcher("*|RB|*")("|RB|Honzik1") shouldBe true
      NicknameMatcher("*|RB|*")("Red|RB|Butcher") shouldBe true
      NicknameMatcher("|w00p")("|w00p") shouldBe false
      NicknameMatcher("*|w00p")("Drakas|w00p") shouldBe true
      NicknameMatcher("*|w00p")("Drakas|xw00p") shouldBe false
    }
  }
}
