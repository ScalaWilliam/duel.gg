
import javax.persistence.{Version, Id}

import com.orientechnologies.orient.`object`.db.OObjectDatabaseTx

class User {
  @Id var id: String = _
  var name: String = _
  var addresses: java.util.List[Address] = new java.util.ArrayList()
  @Version var version: String = _

  override def toString = "User: " + this.id + ", name: " + this.name + ", addresses: " + this.addresses
}

class Address {
  var city: String = _
  var street: String = _

  override def toString = "Address: " + this.city + ", " + this.street
}

class Question {
  @Id var id: String = _
  var title: String = _
  var user: User = _
  @Version var version: String = _

  override def toString = "Question: " + this.id + ", title: " + this.title + ", belongs: " + user.name
}

import com.orientechnologies.orient.core.id.ORecordId
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

object Test {
  implicit def dbWrapper(db: OObjectDatabaseTx) = new {
    def queryBySql[T](sql:String,params:AnyRef*): List[T] = {
      val params4java = params.toArray
      val results: java.util.List[T] = db.query(new OSQLSynchQuery[T](sql), params4java: _*)
      results.asScala.toList
    }
  }

  def main(args: Array[String]) = {
    // ~~~~~~~~~~~~~ create db ~~~~~~~~~~~~~~~~~~~
    var uri: String = "local:test/orientdb"
    var db: OObjectDatabaseTx = new OObjectDatabaseTx(uri)
    if (!db.exists) {
      db.create()
    } else {
      db.open("admin", "admin")
    }

    // ~~~~~~~~~~~~ register models ~~~~~~~~~~~~~~~~
//    db.getEntityManager.registerEntityClasses("models")
    db.getEntityManager.registerEntityClass(classOf[User])
    db.getEntityManager.registerEntityClass(classOf[Address])
    db.getEntityManager.registerEntityClass(classOf[Question])

    // ~~~~~~~~~~~~~ create some data ~~~~~~~~~~~~~~~~
    var user: User = new User
    user.name = "aaa"
    db.save(user)

    var address1 = new Address
    address1.city = "NY"
    address1.street = "road1"
    var address2 = new Address
    address2.city = "ST"
    address2.street = "road2"

    user.addresses += address1
    user.addresses += address2
    db.save(user)

    var q1 = new Question
    q1.title = "How to use orientdb in scala?"
    q1.user = user
    db.save(q1)

    var q2 = new Question
    q2.title = "Show me a demo"
    q2.user = user
    db.save(q2)

    // ~~~~~~~~~~~~~~~~ count them ~~~~~~~~~~~~~~~~
    val userCount = db.countClass(classOf[User])
    println("User count: " + userCount)

    val questionCount = db.countClass(classOf[Question])
    println("Question count: " + questionCount)

    // ~~~~~~~~~~~~~~~~~ get all users ~~~~~~~~~~~~
    val users = db.queryBySql[User]("select from User")
    for (user <- users) {
      println(" - user: " + user)
    }

    // ~~~~~~~~~~~~~~~~~~ get the first user ~~~~~~~~
    val firstUser = db.queryBySql[User]("select from User limit 1").head
    println("First user: " + firstUser)

    // query by id
    val userById = db.queryBySql[User]("select from User where @rid = ?", new ORecordId(user.id))
    println("User by id: " + userById)

    // query by field
    val userByField = db.queryBySql[User]("select from User where name = ?", user.name)
    println("User by field: " + userByField)

    // query by city
    val userByCity = db.queryBySql[User]("select from User where addresses contains ( city = ? )", "NY")
    println("User by city: " + userByCity)

    // query questions of the user
    val questions = db.queryBySql[Question]("select from Question where user = ?",user)
    for (q <- questions) {
      println(" - question: " + q)
    }

    db.drop()
    db.close()
  }
}
