import java.io.File
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.{DB, Options}

object DbInstance {

  def withDb[T](options: Options = new Options())(f: DB => T):T = {
//    val fn = "/home/william/Projects/14/ladder.sauer/tmp/run-pinger-service/sample-data"
    val fn = """/home/william/Projects/14/ladder.sauer/tmp/0410"""
    val db = factory.open(new File(fn), options)
    try {
      f(db)
    } finally {
      db.close()
    }
  }

}
