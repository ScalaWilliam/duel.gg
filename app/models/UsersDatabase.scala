package models

import javax.inject._

import org.h2.mvstore.MVStore
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.{Future, ExecutionContext}

/**
 * Created on 15/08/2015.
 */
@Singleton
class UsersDatabase @Inject()(applicationLifecycle: ApplicationLifecycle, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  import controllers.writeUser

  private def filename = configuration.getString("gg.duel.users.database.file").getOrElse{
    throw new RuntimeException("database filename not specified")
  }

  private def mapName = configuration.getString("gg.duel.users.database.map").getOrElse{
    throw new RuntimeException("map name not specified")
  }

  private val db = MVStore.open("users.db")

  def getUsersMap = db.openMap[String, String]("users")

  def getUsers = {
    Users(
      users = getUsersMap.asScala.map { case (key, value) =>
        UserId(key) -> Json.fromJson[FullUser](Json.parse(value)).get
      }.toMap)
  }

  def putUser(fullUser: FullUser): Unit = {
    getUsersMap.put(fullUser.id, Json.stringify(Json.toJson(fullUser)))
    db.commit()
  }

  applicationLifecycle.addStopHook(() => Future {
    db.close()
  })
}
