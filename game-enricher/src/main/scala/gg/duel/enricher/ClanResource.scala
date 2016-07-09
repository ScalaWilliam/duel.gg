package gg.duel.enricher

/**
  * Created by me on 09/07/2016.
  */
object ClanResource {

  case class ClanRep(name: String, tag: String, website: Option[String])

  val clans: List[ClanRep] = {
    val src = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/clans.tsv"))
    try src.getLines().map(_.split("\t")).collect {
      case Array(n, t) => ClanRep(n, t, None)
      case Array(n, t, w) => ClanRep(n, t, Some(w))
    }.toList
    finally src.close()
  }

}
