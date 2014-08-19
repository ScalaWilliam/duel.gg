package us

import javax.xml.namespace.QName

import com.xqj2.XQConnection2
import org.scalatest.{WordSpec, Matchers}
import us.woop.pinger.analytics.DuelMaker.CompletedDuel
import us.woop.pinger.data.journal.IterationMetaData

class BaseXPersisterSpec extends WordSpec with Matchers {


  "Duel pusher" must {
    val ds =  new net.xqj.basex.local.BaseXXQDataSource()
    val sampleDuel = CompletedDuel.test.toSimpleCompletedDuel.copy(metaId=Option(IterationMetaData.build.id)).copy(duration = 15)
    val dbName = "lozlzozlolozol"
    val conn = ds.getConnection.asInstanceOf[XQConnection2]
    val xqe = conn.createExpression()
    xqe.executeCommand(s"CHECK $dbName")
    xqe.executeCommand(s"CLOSE")
    xqe.executeCommand(s"DROP DB $dbName")
    xqe.executeCommand(s"CHECK $dbName")
    val sda = new us.SimpleBaseXPerister(dbName, conn, "lozlzozlolozol")
    "Return a node that is put in" in {
      import BaseXPersister.Implicits._
      val ps = conn.prepareExpression("declare variable $lol as node() external; $lol")
      ps.bindNode(new QName("lol"), <what/>.asJava, null)
      val res = ps.executeQuery()
      res.next() shouldBe true
      res.getNode.asScala shouldBe <what/>
    }
    "List no duels" in {
      sda.listDuels shouldBe empty
    }
    "Insert a duel when one does not exist and give it an ID" in {
      try {
        val result = sda.pushDuel(sampleDuel, IterationMetaData.build)
        val duel = sda.getDuel(result)
        println(duel)
        sda.listDuels should have size 1
      } finally {

        println(sda.listDuels)
      }
    }
    "Do nothing if a duel already exists" in {
      val result = sda.pushDuel(sampleDuel, IterationMetaData.build)
      val duel = sda.getDuel(result)
      println(duel)
      sda.listDuels should have size 1
    }
  }
}
