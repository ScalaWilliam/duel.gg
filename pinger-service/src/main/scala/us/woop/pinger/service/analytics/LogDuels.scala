package us.woop.pinger.service.analytics

import akka.actor.ActorDSL._
import us.woop.pinger.analytics.DuelMaker.CompletedDuel
import us.woop.pinger.data.journal.IterationMetaData

class LogDuels extends Act {

}
object Wutwut extends App  {
  val xml = CompletedDuel.test.toSimpleCompletedDuel.copy(metaId = Option(IterationMetaData.build.id)).toXml
  println(xml)
}
