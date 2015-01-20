package gg.duel.pinger.data.journal

import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scala.util.Try

case class IterationMetaData
(id: String, unixTime: Long, timestamp: String,
 processStartTime: Long, processStartTimestamp: String,
 processName: String, commitId: Option[String], cliOptions: Option[List[String]],
 user: String, machineName: String,
 version: Option[String], currentDir: String, source: Option[String] = None, finishedOn: Option[Long] = None) {

  def withNewId = copy(id = IterationMetaData.newId())

  def withRecordStartTime(time: Long) = {
    val date = new Date(time)
    val dateFormat = ISODateTimeFormat.dateTimeNoMillis
    copy(
      id = IterationMetaData.newId(date),
      unixTime = time,
      timestamp = dateFormat.print(new DateTime(time))
    )
  }

  def completed =
    copy(finishedOn = Option(System.currentTimeMillis))

  def toXml = {
    <meta
    id={id}
    unix-time={unixTime.toString}
    timestamp={timestamp}
    process-start-time={processStartTime.toString}
    process-start-timestamp={processStartTimestamp}
    process-name={processName}
    commit-id={commitId.orNull}
    user={user}
    machine-name={machineName}
    version={version.orNull}
    current-dir={currentDir}
    source={source.orNull}
    finishedOn={finishedOn.map(_.toString).orNull}
    cli-options={cliOptions.map(_.mkString(" ")).orNull}/>
  }

  def toJson: String = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.writePretty
    implicit val formats = Serialization.formats(NoTypeHints)
    writePretty(this)
  }
}

object IterationMetaData {

  def fromJson(json: String): IterationMetaData = {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.read
    implicit val formats = Serialization.formats(NoTypeHints)
    read[IterationMetaData](json)
  }
  def newId(date: Date = new Date) = {
    val uuid = java.util.UUID.randomUUID().toString
    val dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm")
    val dateString = dateFormat.format(date)
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
    IterationMetaData(
      id = newId(),
      unixTime = unixTime,
      timestamp = timestamp,
      processStartTime = processStartTime,
      processStartTimestamp = processStartTimestamp,
      processName = processName,
      commitId = commitIdO,
      cliOptions = Option(cliOptions).filterNot(_.isEmpty),
      user = user,
      machineName = hostname,
      version = metadataVersion,
      currentDir = currentDir
    )
  }
}