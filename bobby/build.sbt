import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper._

scalaVersion := "2.11.2"

packageArchetype.java_application

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

name := "bobby"

version in ThisBuild := "0.9-" + versionKey.value

mapGenericFilesToLinux

crossPaths := false

publishArtifact in (Compile, packageBin) := false

publishArtifact in (Universal, packageZipTarball) := true

makeDeploymentSettings(Universal, packageZipTarball in Universal, "tgz")

libraryDependencies += "org.pircbotx" % "pircbotx" % "2.0.1"

libraryDependencies += "com.hazelcast" % "hazelcast-client" % "3.2.5"

// packager only accepts one App inside the project. Multiple 'Apps' mess things up.

libraryDependencies ++= {
  val akkaV = "2.3.5"
  val sprayV = "1.3.1"
  Seq(
    "io.spray"            %%  "spray-client"     % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test"
  )
}

mainClass := Some("us.woop.pinger.bobby.BobbyApp")

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

lazy val versionKey = settingKey[String]("Version key")

versionKey := {
  import org.eclipse.jgit.revwalk.RevWalk
  import org.joda.time.format.DateTimeFormatterBuilder
  import org.joda.time.{DateTimeZone, DateTime}
  import org.eclipse.jgit.storage.file.FileRepositoryBuilder
  import org.eclipse.jgit.lib._
  import org.eclipse.jgit.api.Git
  val repository = new FileRepositoryBuilder().readEnvironment.findGitDir.build
  val head = repository.getRef(Constants.HEAD)
  val timezonedDateTime = {
    val revWalk = new RevWalk(repository)
    val rootCommit = revWalk.parseCommit(head.getObjectId)
    val authorIdent = rootCommit.getAuthorIdent
    val authorDate = authorIdent.getWhen
    val authorTimeZone = authorIdent.getTimeZone
    new DateTime(authorDate).withZone(DateTimeZone.forTimeZone(authorTimeZone))
  }
  val shortDate = {
    val dtf = new DateTimeFormatterBuilder()
      .appendMonthOfYear(2)
      .appendDayOfMonth(2)
      .appendHourOfDay(2)
      .toFormatter.withZone(DateTimeZone.UTC)
    dtf.print(timezonedDateTime)
  }
  val revisionId = head.getObjectId.abbreviate(6).name()
  val cleanness = {
    val isClean = new Git(repository).status.call.isClean
    if ( isClean ) "clean" else "dirty"
  }
  s"$shortDate-$revisionId-$cleanness"
}
