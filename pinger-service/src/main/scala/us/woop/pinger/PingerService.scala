package us.woop.pinger

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.xqj2.XQConnection2
import net.xqj.basex.BaseXXQDataSource

object PingerService extends App with StrictLogging {

  def basexConnection = {
    val xqs = new BaseXXQDataSource() {
      setProperty("serverName", "localhost")
      setProperty("port", "1984")
      setProperty("databaseName", "matches")
    }
    xqs.getConnection("pingerpersist", "awesome").asInstanceOf[XQConnection2]
//    xqs.getConnection("admin", "admin").asInstanceOf[XQConnection2]
  }

}
