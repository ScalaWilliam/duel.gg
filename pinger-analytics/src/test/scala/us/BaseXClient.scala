package us

import javax.xml.xquery.XQConnection

import net.xqj.basex.BaseXConnectionPoolXQDataSource
import net.xqj.pool.PooledXQDataSource
import scala.concurrent.{Await, Future}

object BaseXClient extends App {

  val cpds = new BaseXConnectionPoolXQDataSource()

  cpds.setProperty("user", "admin")
  cpds.setProperty("password", "admin")

  def withConnection[T](f: XQConnection => T): T = {
    val q = cpds.getPooledConnection.getConnection
    try {
      f(q)
    } finally {
      q.close()
    }
  }

  def testResult = withConnection { q =>
    val result = q.prepareExpression("<test/>").executeQuery()
    try {
      result.next()
      result.getSequenceAsString(null)
    } finally {
      result.close()
    }
  }

  import concurrent.ExecutionContext.Implicits.global
  val start = System.currentTimeMillis()
  val res = for { i <- (1 to 500) } yield Future{testResult}
  val fr = Future.sequence(res)

  import concurrent.duration._
  println(Await.result(fr, 5.minutes))
  val end = System.currentTimeMillis()
  println((end-start).millis)


  println(testResult)

}
