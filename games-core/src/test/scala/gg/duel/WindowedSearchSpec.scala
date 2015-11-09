package gg.duel

import org.scalatest.{Matchers, Inside, OptionValues, WordSpec}

class WindowedSearchSpec
  extends WordSpec
  with Inside
  with OptionValues
  with Matchers {

  "Side biased lookup" must {
    "Work when no games are available" in {
      val sbl = WindowedSearch[Char](Vector.empty).SideBiasedLookup(limit = 3)
      sbl.FirstBiasedLookup.apply().items shouldBe empty
      sbl.FirstBiasedLookup.apply().laterFocus shouldBe empty
      sbl.LastBiasedLookup.apply().items shouldBe empty
      sbl.LastBiasedLookup.apply().previousFocus shouldBe empty
    }
    "Work when too few games are available" in {
      val sbl = WindowedSearch[Char](('a' to 'b').toVector).SideBiasedLookup(limit = 3)
      sbl.FirstBiasedLookup.apply().items shouldBe Vector('a', 'b')
      sbl.FirstBiasedLookup.apply().laterFocus shouldBe empty
      sbl.LastBiasedLookup.apply().items shouldBe Vector('b', 'a')
      sbl.LastBiasedLookup.apply().previousFocus shouldBe empty
    }
  }
  
  "Focused Lookup" must {
    def focused[T](at: Char, lim: Int = 3): (
      Option[WindowedSearch[Char]#FocusedLookup#SingleFilteringResult],
      Option[WindowedSearch[Char]#FocusedLookup#MultipleLookup#BeforeLookupResult],
      Option[WindowedSearch[Char]#FocusedLookup#MultipleLookup#AfterLookupResult]
      ) = {
      val ws = WindowedSearch(('a' to 'z').toVector).FocusedLookup(_ == at)
      val mbl = ws.MultipleLookup(lim)
      val sl = ws.SingleLookup()
      val bef = mbl.BeforeLookup()
      val aft = mbl.AfterLookup()
      (sl, bef, aft)
    }
    "Look up correctly by huge range" in {
      inside(focused(at = 'a', lim = 500)) {
        case (_, _, Some(after)) =>
          after.focus shouldBe 'a'
          after.next should contain only ('b' to 'z' :_*)
          after.laterGame.value shouldBe 'z'
      }
      inside(focused(at = 'z', lim = 500)) {
        case (_, Some(before), _) =>
          before.focus shouldBe 'z'
          before.previous should contain only ('a' to 'y' :_*)
          before.earlierGame shouldBe empty
      }
    }
    "Look up correctly by first item" in {
      inside(focused(at = 'a')) {
        case (Some(focused), beforeO, Some(a)) =>
          focused.focus shouldBe 'a'
          focused.next.value shouldBe 'b'
          focused.previous shouldBe empty

          beforeO shouldBe empty

          a.focus shouldBe 'a'
          a.next should contain only ('b', 'c', 'd')
          a.laterGame.value shouldBe 'd'
      }
    }
    "Look up correctly by second item" in {
      inside(focused(at = 'b')) {
        case (Some(focused), Some(before), Some(after)) =>
          focused.focus shouldBe 'b'
          focused.next.value shouldBe 'c'
          focused.previous.value shouldBe 'a'

          before.focus shouldBe 'b'
          before.earlierGame shouldBe empty
          before.previous should contain only ('a')

          after.focus shouldBe 'b'
          after.next should contain only ('c' to 'e' :_*)
          after.laterGame.value shouldBe 'e'
      }
    }
    "Look up correctly by middle item" in {
      inside(focused(at = 'm')) {
        case (Some(focused), Some(before), Some(after)) =>
          focused.focus shouldBe 'm'
          focused.next.value shouldBe 'n'
          focused.previous.value shouldBe 'l'

          before.focus shouldBe 'm'
          before.earlierGame.value shouldBe 'j'
          before.previous should contain only ('j','k','l')

          after.focus shouldBe 'm'
          after.next should contain only ('n', 'o', 'p')
          after.laterGame.value shouldBe 'p'
      }
    }
    "Look up correctly by last item" in {
      inside(focused(at = 'z')) {
        case (Some(focused), Some(before), Some(after)) =>
          focused.focus shouldBe 'z'
          focused.next shouldBe empty
          focused.previous.value shouldBe 'y'

          before.focus shouldBe 'z'
          before.earlierGame.value shouldBe 'w'
          before.previous should contain only ('w','x','y')

          after.focus shouldBe 'z'
          after.next shouldBe empty
          after.laterGame shouldBe empty
      }
    }
    "Look up correctly by penultimate item" in {
      inside(focused(at = 'y')) {
        case (Some(focused), Some(before), Some(after)) =>
          focused.focus shouldBe 'y'
          focused.next.value shouldBe 'z'
          focused.previous.value shouldBe 'x'

          before.focus shouldBe 'y'
          before.earlierGame.value shouldBe 'v'
          before.previous should contain only ('v','w','x')

          after.focus shouldBe 'y'
          after.next should contain only('z')
          after.laterGame.value shouldBe 'z'
      }
    }
  }


}