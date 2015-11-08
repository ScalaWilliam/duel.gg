package services.games

import slick.driver.PostgresDriver.api._

import scala.language.implicitConversions

/**
  * Created by William on 08/11/2015.
  */
class GamesTable(tag: Tag) extends Table[(String, String)](tag, "GAMES") {
  def id = column[String]("ID", O.PrimaryKey)

  def json = column[String]("JSON")

  def * = (id, json)
}
