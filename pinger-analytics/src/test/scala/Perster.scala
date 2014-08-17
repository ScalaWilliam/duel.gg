import javax.xml.xquery.XQPreparedExpression

import us.woop.pinger.analytics.DuelMaker
import us.woop.pinger.analytics.DuelMaker.CompletedDuel

object Perster extends App {
  import java.util.Properties
  import javax.xml.namespace.QName
  import javax.xml.xquery.XQItemType
  import com.xqj2.XQConnection2
  import DuelMaker.CompletedDuel
  import org.basex.BaseXClient
  import scala.xml.PrettyPrinter

  val conn =  new net.xqj.basex.local.BaseXXQDataSource().getConnection.asInstanceOf[XQConnection2]

  val yes = CompletedDuel.test.toSimpleCompletedDuel.copy(duration = 15).toXml

  val db = "duels"

  val xqe = conn.createExpression()

  xqe.executeCommand(s"CHECK $db")


  xqe.executeCommand("SET DEFAULTDB true")

  val exp = conn.prepareExpression(
    """
      |declare variable $exp as xs:string external;
      |xquery:eval($exp)
    """.stripMargin)





  exp.bindString(new QName("exp"), s"$yes", null)

//  conn.prepareExpression("""db:create("duels")""").executeQuery()

  val addDuel = conn.prepareExpression(
  s"""declare variable $$duel as xs:string external;
     |let $$duelX := xquery:eval($$duel)
     |return db:add("duels", $$duelX, "duelz.xml")
   """.stripMargin
  )

  addDuel.bindString(new QName("duel"), s"$yes", null)

//  addDuel.executeQuery()

  val readDuels = conn.prepareExpression(
  s"""
     |for $$doc in db:open("duels")
     |return $$doc
   """.stripMargin)
  println(readDuels.executeQuery().getSequenceAsString(new Properties()))

  val result = exp.executeQuery()
  val ah = result.getSequenceAsString(new Properties())
//  println(ah)

  conn.close()

}
