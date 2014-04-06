package us.woop.pinger.persistence

import com.datastax.driver.core.Session
import us.woop.pinger.SauerbratenServerData._
import scala.util.control.NonFatal
import us.woop.pinger.SauerbratenServerData.Conversions._

object TableCreation {

  import CqlInterfacing.AbstractCqlInterface

  implicit class addCreationToSession(session: Session) {
    def createDefaultTables = {
      import StatementGeneratorImplicits._
      createTable[PlayerCns]
      createTable[PlayerCns]
      createTable[ConvertedServerInfoReply]
      createTable[ConvertedThomasExt]
      createTable[ConvertedTeamScore]
      createTable[Uptime]
      createTable[ConvertedHopmodUptime]
      createTable[PlayerExtInfo]
    }

    def createTable[T <: Product](implicit ev: AbstractCqlInterface[T]) = {
      val createQuery = implicitly[AbstractCqlInterface[T]].makeCreateQuery
      try {
        session.execute(createQuery)
      } catch {
        case NonFatal(e) =>
          throw new RuntimeException(s"Failed with query $createQuery -- $e", e)
      }
    }
  }
}
