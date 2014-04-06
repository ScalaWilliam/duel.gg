package us.woop.pinger.client

object FlatteningAbstracts {

  trait Process[T, V] {
    def process(input: T): Seq[V]
  }

  abstract class ProcessToMany[T, V](processor: T => Seq[V]) extends Process[T, V] {
    def process(input: T) = processor apply input
  }

  abstract class ProcessToDifferent[T, V](processor: T => V) extends Process[T, V] {
    def process(input: T) = Seq(processor apply input)
  }

}


