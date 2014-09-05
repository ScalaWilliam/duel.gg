import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper._

//import play.Project._

name := "duelgg-frontend"

libraryDependencies += "com.hazelcast" % "hazelcast-client" % "3.2.5"

//playScalaSettings

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies += "com.github.scala-incubator.io" % "scala-io-core_2.11" % "0.4.3-1"

libraryDependencies += "com.github.scala-incubator.io" % "scala-io-file_2.11" % "0.4.3-1"

libraryDependencies += "com.hazelcast" % "hazelcast" % "3.2.5"

libraryDependencies += ws

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

crossPaths := false

version in ThisBuild := "0.9-" + versionKey.value

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
      //      .appendTwoDigitYear(2000)
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

publishArtifact in (Compile, packageBin) := false

publishArtifact in (Universal, packageZipTarball) := true

makeDeploymentSettings(Universal, packageZipTarball in Universal, "tgz")