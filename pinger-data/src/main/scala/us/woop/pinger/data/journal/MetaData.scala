package us.woop.pinger.data.journal

import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import com.typesafe.config.ConfigFactory
import org.joda.time.format.ISODateTimeFormat

import scala.util.Try

case class MetaData
(id: String, unixTime: Long, timestamp: String,
 processStartTime: Long, processStartTimestamp: String,
 processName: String, commitId: Option[String], cliOptions: List[String],
 user: String, machineName: String,
 version: Option[String], currentDir: String) {

  def withNewId = copy(id = MetaData.newId)

  def toJson = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.writePretty
    implicit val formats = Serialization.formats(NoTypeHints)
    writePretty(this)
  }
}

object MetaData {

  def newId = {
    val uuid = java.util.UUID.randomUUID().toString
    val dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm")
    val dateString = dateFormat.format(new Date)
    s"sb-$dateString-${uuid.take(8)}"
  }
  
  val metadataVersion = Option("2014-08-15")
  
  def build = {
    import collection.JavaConverters._
    val processStartTime = ManagementFactory.getRuntimeMXBean.getStartTime
    val cliOptions = ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toList
    val dateFormat = ISODateTimeFormat.dateTimeNoMillis
    val timestamp = dateFormat.print(org.joda.time.DateTime.now)
    val processStartTimestamp = dateFormat.print(processStartTime)
    val unixTime = System.currentTimeMillis()
    val hostname = InetAddress.getLocalHost.getHostName
    val currentDir = scala.util.Properties.userDir
    val user = scala.util.Properties.userName
    val processName = ManagementFactory.getRuntimeMXBean.getName
    val commitIdO = Try(ConfigFactory.load("/git.properties").getString("git.commit.id")).toOption.flatMap(Option.apply)
    MetaData(
      id = newId,
      unixTime = unixTime,
      timestamp = timestamp,
      processStartTime = processStartTime,
      processStartTimestamp = processStartTimestamp,
      processName = processName,
      commitId = commitIdO,
      cliOptions = cliOptions,
      user = user,
      machineName = hostname,
      version = metadataVersion,
      currentDir = currentDir
    )
  }
}