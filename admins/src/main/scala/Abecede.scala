import com.orientechnologies.orient.client.remote.OServerAdmin
import com.orientechnologies.orient.core.command.OCommandResultListener
import com.orientechnologies.orient.core.db.document.{ODatabaseDocumentTxPooled, ODatabaseDocumentPool, ODatabaseDocumentTx}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery

object Abecede extends App {

  //  val serverAdmin: OServerAdmin = new OServerAdmin("remote:localhost/stuff").connect("root", "43D83BD2B2F3FC652F9A48F422B4BAA575032277F1B3315C015CCCC05838FD1F")
  //  println(serverAdmin.createDatabase("stuff", "local"))
  //  serverAdmin.close()
  //  val db: ODatabaseDocumentTx = ODatabaseDocumentPool.global().acquire("remote:localhost/stuff", "admin", "admin")
  ////  val db: ODatabaseDocumentTx = new ODatabaseDocumentTx("remote:localhost/stuff").open("admin", "admin")
  //try {
  //} finally {
  //  db.close()
  //}
  //
  //  val doc = new ODocument("Wawawaw")
  //  doc.field("name", "Luke")
  //  doc.field("surname", "Skywalker")
  //  doc.field("city", new ODocument("City").field("name", "Rome").field("country", "Italy"))
  //  println(doc.save())

  val db: ODatabaseDocumentTx = new ODatabaseDocumentTx("remote:localhost/stuff").open("admin", "admin")
  db.command[OSQLAsynchQuery[ODocument]](
    new OSQLAsynchQuery[ODocument]("select * from OUser",
      new OCommandResultListener() {
        var resultCount: Int = 0
        @Override
        override def result(iRecord: Any): Boolean = {
          resultCount = resultCount + 1
          val doc = iRecord.asInstanceOf[ODocument]
          println(doc)
          resultCount > 20
        }
        @Override
        override def end(): Unit = {
        }
      })).execute()
  db.close()
}
