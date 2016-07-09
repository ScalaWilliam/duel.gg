package gg.duel.enricher

import gg.duel.enricher.lookup.LookingUp
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by me on 09/07/2016.
  */
class EnricherTest extends FunSuite with Matchers {
  test("It works") {
    val src = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/test-journal.txt"))
    val listedGames =
      try src.getLines.flatMap(_.split("\t").drop(1).headOption).toList
      finally src.close()
    val updatedGames = listedGames.map {
      g => val n = GameNode(jsonString = g, plainGameEnricher = LookingUp.mock)
        n.Mutations.enrich()
        n.asPrettyJson
    }
    updatedGames.foreach(println)
  }
}
