package us.woop.pinger.persistence

import us.woop.pinger.PingerServiceData.SauerbratenPong

object PayloadFlattening {

  import CqlInterfacing.AbstractCqlInterface

  trait Process[T, V <: Product] {
    def process(input: T): Seq[V]

    def intoPair(pong: SauerbratenPong, input: T)(implicit resolver: AbstractCqlInterface[V]): Seq[(String, List[Any])] = {
      val insertQuery = resolver.makeInsertQuery
      for {
        item <- process(input)
      } yield (insertQuery, resolver.stmtValues(pong, item))
    }
  }

  abstract class ProcessToMany[T, V<:Product](processor: T => Seq[V]) extends Process[T, V] {
    def process(input: T) = processor apply input
  }
  abstract class ProcessToDifferent[T, V<:Product](processor: T => V) extends Process[T, V] {
    def process(input: T) = Seq(processor apply input)
  }

  trait Identity[T <: Product] extends Process[T, T] {
    this: Process[T, T] =>
    def process(input: T) = Seq(input)
  }


}
