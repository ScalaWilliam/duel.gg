package gg.duel.query

import gg.duel.query.SimpleFocusResult.SFRJS
import play.api.libs.json.{JsValue, JsNull, Json, Writes}

import scala.language.higherKinds

/**
 * Created on 30/10/2015.
 */
sealed trait Focus {
  type Result[_]
  def collect[T](previous: Vector[T], focus: T, next: Vector[T]): Result[T]
}

case object SimpleFocus extends Focus {
  override type Result[_] = SimpleFocusResult[_]
  override def collect[T](previous: Vector[T], focus: T, next: Vector[T]): SimpleFocusResult[T] =
    SimpleFocusResult(
      previous = previous.headOption,
      focus = focus,
      next = next.headOption
    )
}

sealed trait MultipleFocus extends Focus {
  override type Result[_] = MultipleFocusResult[_]
  override def collect[T](previous: Vector[T], focus: T, next: Vector[T]): MultipleFocusResult[T]
}

case class RadialFocus(radius: Int) extends MultipleFocus {
  override def collect[T](previous: Vector[T], focus: T, next: Vector[T]): MultipleFocusResult[T] =
    MultipleFocusResult(
      previous = Option(previous.take(radius)).filter(_.nonEmpty),
      next = Option(next.take(radius)).filter(_.nonEmpty),
      focus = focus
    )
}

case class AsymmetricFocus(previous: Int, next: Int) extends MultipleFocus {
  af =>
  override def collect[T](previous: Vector[T], focus: T, next: Vector[T]): MultipleFocusResult[T] =
    MultipleFocusResult(
      previous = Option(previous.take(af.previous)).filter(_.nonEmpty),
      next = Option(next.take(af.next)).filter(_.nonEmpty),
      focus = focus
    )
}

case class SimpleFocusResult[T](focus: T, previous: Option[T], next: Option[T])
object SimpleFocusResult {
  private case class SFRJS(game: JsValue, previous: Option[JsValue], next: Option[JsValue])
  private implicit val sfrjsWrites = Json.writes[SFRJS]
  implicit def sfrWrites[T](implicit writesT: Writes[T]): Writes[SimpleFocusResult[T]] = new Writes[SimpleFocusResult[T]] {
    override def writes(o: SimpleFocusResult[T]): JsValue = {
      sfrjsWrites.writes(SFRJS(
        game = writesT.writes(o.focus),
        previous = o.previous.map(writesT.writes),
        next = o.next.map(writesT.writes)
      ))
    }
  }
}

case class MultipleFocusResult[T](focus: T, previous: Option[Vector[T]], next: Option[Vector[T]])
object MultipleFocusResult {
  private case class MFRJS(game: JsValue, previous: Option[Vector[JsValue]], next: Option[Vector[JsValue]])
  private implicit val sfrjsWrites = Json.writes[MFRJS]
  implicit def sfrWrites[T](implicit writesT: Writes[T]): Writes[MultipleFocusResult[T]] = new Writes[MultipleFocusResult[T]] {
    override def writes(o: MultipleFocusResult[T]): JsValue = {
      sfrjsWrites.writes(MFRJS(
        game = writesT.writes(o.focus),
        previous = o.previous.map(_.map(writesT.writes)),
        next = o.next.map(_.map(writesT.writes))
      ))
    }
  }
}