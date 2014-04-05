package us.woop.pinger.persistence

import java.util.UUID
import us.woop.pinger.PingerServiceData.SauerbratenPong

object StatementGeneration {

  import reflect.runtime.universe._

  def getMethods[T: TypeTag] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList

  abstract class StatementGenerator[OfType <: Product](implicit tt: TypeTag[OfType]) {

    protected def typeFields = getMethods[OfType]

    protected def payloadFields: List[(String, String)] = {
      typeFields map {
        method =>
          val returnType = method.returnType match {
            case f if f =:= typeOf[String] => "text"
            case f if f =:= typeOf[Int] => "int"
            case f if f =:= typeOf[Boolean] => "boolean"
            case f if f =:= typeOf[UUID] => "uuid"
            case f if f =:= typeOf[BigInt] => "bigint"
            case f if f =:= typeOf[List[Int]] => "list<int>"
          }
          val name = method.name.decodedName.decoded
          (name, returnType)
      }
    }.toList

    {
      payloadFields
    }

    def tableName: String

    def headerFields = List("id" -> "uuid", "serverip" -> "text", "serverport" -> "int", "time" -> "bigint")

    def makeInsertQuery = {
      val allFields = headerFields ::: payloadFields
      s"""INSERT INTO $tableName (${
        allFields.map {
          _._1
        }.mkString(", \n")
      }) \nVALUES (${"?" * allFields.size mkString ", "}); """
    }

    def fieldsForCreate = {
      val allFields = headerFields ::: payloadFields
      allFields.map {
        case (a, b) => s"$a $b"
      }.mkString(", \n")
    }

    def stmtValues(sauerbratenPong: SauerbratenPong, payload: OfType) = {
      import sauerbratenPong.host
      import sauerbratenPong.unixTime
      val res = payloadFields.map {
        case (key, typ) => payload.getClass.getMethod(key).invoke(payload)
      }
      import scala.collection.JavaConverters._
      val stuff = List(UUID.randomUUID, host._1, host._2, unixTime) ::: res.collect {
        case l: List[_] => l.asJava
        case other => other
      }.toList
      stuff.map {
        _.asInstanceOf[Object]
      }
    }

    def makeCreateQuery =
      s"""CREATE TABLE IF NOT EXISTS $tableName ($fieldsForCreate, PRIMARY KEY(id));"""

  }


}
