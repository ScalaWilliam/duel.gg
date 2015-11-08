package services

import slick.driver.PostgresDriver.api._

class GamesTable(tag: Tag) extends Table[(String, String)](tag, "GAMES") {
  def id = column[String]("ID", O.PrimaryKey)

  def json = column[String]("JSON")

  def * = (id, json)
}
