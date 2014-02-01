//
//object Parseme extends App {
//
//  object ExtractionApi {
//
//    case class ParseResult[T, V](value: T, rest: Stream[V])
//
//    def parseOne[T, V](input: Stream[V])(implicit f: Stream[V] => Option[(T, Stream[V])]): Option[ParseResult[T, V]] =
//      for { (result, rest) <- f(input) } yield ParseResult(result, rest)
//
//    def parseMany[T, V](input: Stream[V])(implicit f: Stream[V] => Option[(T, Stream[V])]): ParseResult[Stream[T], V] = {
//      parseOne(input) match {
//
//      }
//      def fib(a: Int, b: Int): Stream[Int] = a #:: fib(b, a + b)
//
//      def recurse(left: Stream[ParseResult[Stream[T], V]]], input: Stream[V]): Stream[ParseResult[T, V]] = {
//        left.
//        parseOne(input).toStream #::: recurse
//      }
//      recurse(input).last
//    }
//    private def parseMany[T, V](accumulated: Stream[T], input: Stream[V])(implicit f: Stream[V] => Option[(T, Stream[V])]): Stream[ParseResult[Stream[T], V]] = {
//      parseOne(input) match {
//        case Some(ParseResult(value, rest)) =>
//          parseMany(value #:: accumulated, input)
//        case None =>
//          Stream.continually(ParseResult(accumulated.reverse, input)).take(1)
//      }
//    }
//
//    object Dsl {
//      case class Countable(value: Either[Unit, Int])
//      val Many = Countable(Left(Unit))
//      implicit def toCount(n: Int): Countable = Countable(Right(n))
//      implicit class extractStreamValue[T,V](stream: Stream[ParseResult[T,V]]) {
//        def value: List[T] = stream.map(_.value).toList
//      }
//      implicit class toAfterable[V, T](f: Stream[T]=>Stream[ParseResult[V, T]]) {
//        def afterable(implicit countable: Countable) = new {
//          def after(what: ParseResult[_, T]) = {
//            val data = f apply what.rest
//            countable match {
//              case Countable(Right(n)) =>
//                val values = data.take(n).map(_.value)
//                val rest = data.last.rest
//                Stream.continually(ParseResult(values, rest))
//              case _ => data
//            }
//          }
//          def after(what: Stream[ParseResult[_, T]]) = {
//            val data = f apply what.last.rest
//            countable match {
//              case Countable(Right(n)) => data.take(n)
//              case _ => data
//            }
//          }
//        }
//      }
//    }
//
//  }
//
//
//  object CubeProtocol {
//
//    import ExtractionApi._
//
//    val ints: Stream[Int] => Stream[ParseResult[Int, Int]] =
//      pick(_) {
//        case a #:: b #:: c #:: rest if a == 128 =>
//          Option(ParseResult(b | (c << 8), rest))
//        case a #:: b #:: c #:: d #:: e #:: rest if a == 129 =>
//          Option(ParseResult(((b | ( c << 8 )) | (d << 16)) | (e << 24),rest))
//        case a #:: rest => Option(ParseResult(a, rest))
//        case Stream.Empty => None
//      }
//
//    val strings: Stream[Int] => Stream[ParseResult[String, Int]] =
//      pick(_) { stream =>
//        val (haveInts, rest) = ints(stream).span(_.value != 0)
//        Option(ParseResult[String, Int](haveInts.map(_.value).mkString, haveInts.reverse.take(1).flatMap(_.rest.drop(1))))
//      }
//
//    object Dsl {
//      import ExtractionApi.Dsl._
//      implicit class finites(n: Int) {
//        implicit val c: Countable = n
//        def ints = CubeProtocol.ints.afterable
//        def int = CubeProtocol.ints.afterable
//        def strings = CubeProtocol.strings.afterable
//        def string = CubeProtocol.strings.afterable
//      }
//      implicit class infinites(fromCountable: Countable) {
//        implicit val c = fromCountable
//        def ints = CubeProtocol.ints.afterable
//        def strings = CubeProtocol.strings.afterable
//      }
//      val int = 1.int
//      val string = 1.string
//    }
//  }
//
//  implicit class prepareList[T](input: List[T]) {
//    import ExtractionApi._
//    def beginning: ParseResult[Unit, T] =
//      ParseResult[Unit, T](Unit, input.toStream)
//
//  }
//
//  val data = List(1,2,3,4,0,5,2,3,4,0,9,12,41,324,5)
//
//  import ExtractionApi.Dsl._
//  import CubeProtocol.Dsl._
//
//  val ab = for {
//    i <- Many.ints after data.beginning
//  } yield i.value
//  println(ab.toList)
////
////  def extract(from => ..) =
////    for { i <- from } yield i.value
//
//  val output = for {
//    i <- int after data.beginning
//    twoStrings <- 2.strings after i
//    //twoStrings = for { j <- 2.strings after i } yield j
//    oneInt <- 1.int after twoStrings
//    k = Many.ints after oneInt
//  } yield (i.value, twoStrings.value.toList, oneInt.value, k.value.toList)
//
//  println(output.headOption)
//
//}
////
////
////  class Calculator(val input: ParserInput) extends Parser {
////    def InputLine = rule { Expression ~ EOI }
////
////    def Expression: Rule1[Int] = rule {
////      Term ~ zeroOrMore(
////        '+' ~ Term ~> ((_: Int) + _)
////          | '-' ~ Term ~> ((_: Int) - _))
////    }
////
////    def Term = rule {
////      Factor ~ zeroOrMore(
////        '*' ~ Factor ~> ((_: Int) * _)
////          | '/' ~ Factor ~> ((_: Int) / _))
////    }
////
////    def Factor = rule { Number | Parens }
////
////    def Parens = rule { '(' ~ Expression ~ ')' }
////
////    def Number = rule { capture(Digits) ~> (_.toInt) }
////
////    def Digits = rule { oneOrMore(CharPredicate.Digit) }
//
//
//    import org.parboiled2._
//
//  import scala.util.parsing.combinator._
////
////  class JSON extends JavaTokenParsers {
////
////    def value : Parser[Any] = obj | arr |
////      stringLiteral |
////      floatingPointNumber |
////      "null" | "true" | "false"
////
////    def obj   : Parser[Any] = "{" ~ repsep(member, ",")~"}"
////
////    def arr   : Parser[Any] = "["~repsep(value, ",")~"]"
////
////    def member: Parser[Any] = stringLiteral~":"~value
////  }
////
////
////  /** A parser that matches a literal string */
////  implicit def literal(s: String): Parser[String] = new Parser[String] {
////    def apply(in: Input) = {
////      val source = in.source
////      val offset = in.offset
////      val start = handleWhiteSpace(source, offset)
////      var i = 0
////      var j = start
////      while (i < s.length && j < source.length && s.charAt(i) == source.charAt(j)) {
////        i += 1
////        j += 1
////      }
////      if (i == s.length)
////        Success(source.subSequence(start, j).toString, in.drop(j - offset))
////      else  {
////        val found = if (start == source.length()) "end of source" else "`"+source.charAt(start)+"'"
////        Failure("`"+s+"' expected but "+found+" found", in.drop(start - offset))
////      }
////    }
////  }
////
////  def int: Parsers = new Parsers {
////
////    def unapply(buffer: List[Int]): Option[(Int, List[Int])] =
////      buffer match {
////        case a :: b :: c :: rest if a == 128 =>
////          Option(b | (c << 8), rest)
////        case a :: b :: c :: d :: e :: rest if a == 129 =>
////          Option(
////            ((b | ( c << 8 )) | (d << 16)) | (e << 24),rest
////          )
////        case a :: rest => Option((a, rest))
////        case Nil => None
////      }
////  }
////
////
////
////}}