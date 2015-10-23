package models

import javax.inject._

import org.h2.mvstore.MVStore
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class SimpleEmbeddedDatabase @Inject() (implicit
                                        applicationLifecycle: ApplicationLifecycle,
                                        configuration: Configuration,
                                        executionContext: ExecutionContext) {
  private val database = MVStore.open(configuration.getString("database.file").getOrElse("pinger-service.db"))
  def commit(): Unit = database.commit()
  def get(): MVStore = database
  applicationLifecycle.addStopHook(() => Future {
    database.close()
  })
}





