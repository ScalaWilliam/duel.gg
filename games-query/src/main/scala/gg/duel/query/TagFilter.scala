package gg.duel.query

import gg.duel.query.QueryableGame$


sealed trait TagFilter extends (QueryableGame => Boolean) {
  def toMap: Map[String, Seq[String]]
}
case object EmptyTagFilter extends TagFilter {
  override def apply(simpleGame: QueryableGame): Boolean = true
  override def toMap = Map.empty
}
case class NonEmptyTagFilter(tags: Set[String]) extends TagFilter {
  override def apply(simpleGame: QueryableGame): Boolean = {
    (tags -- simpleGame.tags).isEmpty
  }
  override def toMap = Map("tag" -> tags.toSeq)
}
object TagFilter {
  def apply(map: Map[String, Seq[String]]): TagFilter = {
    map.get("tag") match {
      case Some(tags) if tags.nonEmpty =>
        NonEmptyTagFilter(tags = tags.toSet)
      case _ => EmptyTagFilter
    }
  }
}