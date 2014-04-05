package us.woop.pinger.persistence

import us.woop.pinger.PingerServiceData.SauerbratenPong

object PayloadFlattening {

  import StatementGeneration.StatementGenerator

  trait Process[T, V <: Product] {
    def process(input: T): Seq[V]

    def intoPair(pong: SauerbratenPong, input: T)(implicit resolver: StatementGenerator[V]): Seq[(String, List[Any])] = {
      val insertQuery = resolver.makeInsertQuery
      for {
        item <- process(input)
      } yield (insertQuery, resolver.stmtValues(pong, item))
    }

  }

  trait Identity[T <: Product] extends Process[T, T] {
    this: Process[T, T] =>
    def process(input: T) = Seq(input)
  }

}
