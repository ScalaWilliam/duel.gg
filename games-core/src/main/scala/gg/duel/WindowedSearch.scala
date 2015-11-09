package gg.duel

/**
 * Items are assumed to be sorted.
 */
case class WindowedSearch[T](items: Vector[T]) {

  /**
   * Focus aroudn the first item that matches the predicate
   */
  case class FocusedLookup(p: T => Boolean) {

    private def atIndex[V](pf: PartialFunction[Int, V]): Option[V] = {
      val itemIndex = items.indexWhere(p)
      if ( itemIndex >= 0 ) PartialFunction.condOpt(itemIndex)(pf)
      else Option.empty
    }

    case class SingleFilteringResult(focus: T, previous: Option[T], next: Option[T])

    object SingleLookup {
      def apply(): Option[SingleFilteringResult] = {
        atIndex { case n =>
          SingleFilteringResult(
            focus = items(n),
            items.lift.apply(n - 1),
            items.lift.apply(n + 1)
          )
        }
      }
    }

    case class MultipleLookup(limit: Int) {

      case class AfterLookupResult(focus: T, next: Vector[T], laterGame: Option[T]) {
        def beforeGame: Option[T] = next.headOption
      }

      object AfterLookup {
        def apply(): Option[AfterLookupResult] = {
          atIndex {
            // can always have games after this, ie when stuff updates
            case n =>
              val next = items.slice(n + 1, n + 1 + limit)
              AfterLookupResult(
                focus = items(n),
                next = next.reverse,
                laterGame = next.lastOption
              )
          }
        }
      }

      case class BeforeLookupResult(focus: T, previous: Vector[T], earlierGame: Option[T]) {
        def afterGame: Option[T] = previous.headOption
      }

      object BeforeLookup {
        def apply(): Option[BeforeLookupResult] = {
          // can't have any more pages when size is too small.
          atIndex {
            case n if n > 0 && n <= limit =>
              BeforeLookupResult(
                focus = items(n),
                previous = items.take(n).reverse,
                earlierGame = Option.empty
              )
            case n if n > limit =>
              BeforeLookupResult(
                focus = items(n),
                previous = items.slice(n - limit, n).reverse,
                earlierGame = items.lift(n - limit)
              )
          }
        }
      }

    }

  }

  case class SideBiasedLookup(limit: Int) {
    def hasMore = items.size > limit

    case class FirstBiasedLookupResult(items: Vector[T]) {
      def laterFocus: Option[T] = if (hasMore) items.lastOption else Option.empty
    }

    object FirstBiasedLookup {
      def apply(): FirstBiasedLookupResult = {
        FirstBiasedLookupResult(
          items = items.take(limit)
        )
      }
    }

    case class LastBiasedLookup(items: Vector[T]) {
      def previousFocus = if (hasMore) items.lastOption else Option.empty
    }

    object LastBiasedLookup {
      def apply(): LastBiasedLookup = {
        LastBiasedLookup(
          items = items.takeRight(limit).reverse
        )
      }
    }

  }

}