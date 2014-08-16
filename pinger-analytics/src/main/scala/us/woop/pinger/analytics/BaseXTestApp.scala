package us.woop.pinger.analytics

import java.util.Properties
import javax.xml.namespace.QName
import javax.xml.xquery.XQItemType

import com.xqj2.XQConnection2
import DuelMaker.CompletedDuel

import scala.xml.PrettyPrinter

object BaseXTestApp extends App {
  val conn =  new net.xqj.basex.local.BaseXXQDataSource().getConnection.asInstanceOf[XQConnection2]

  val yes = CompletedDuel.test.toSimpleCompletedDuel.toJson

  val db = "xmlstore"

  val xqe = conn.createExpression()

  xqe.executeCommand(s"CHECK $db")

  xqe.executeCommand("SET DEFAULTDB true")

  val exp = conn.prepareExpression(
      """
        |declare variable $exp as xs:string external;
        |json:parse($exp)
      """.stripMargin)
  exp.bindString(new QName("exp"), yes, null)

  val result = exp.executeQuery()
  val ah = result.getSequenceAsString(new Properties())
  println(ah)

  conn.close()

}
