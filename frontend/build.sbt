import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper._

name := "duelgg-frontend"

enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies += "com.github.scala-incubator.io" % "scala-io-core_2.11" % "0.4.3-1"

libraryDependencies += "com.github.scala-incubator.io" % "scala-io-file_2.11" % "0.4.3-1"

libraryDependencies += "com.google.api-client" % "google-api-client" % "1.19.0"

libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.1"

javacOptions in Compile ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation")

libraryDependencies += "com.google.apis" % "google-api-services-plus" % "v1-rev174-1.19.0"

libraryDependencies += "com.google.apis" % "google-api-services-oauth2" % "v2-rev78-1.19.0"

libraryDependencies += "com.hazelcast" % "hazelcast" % "3.2.5"

libraryDependencies += ws

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

crossPaths := false

version in ThisBuild := "0.9-" + versionKey.value

//lazy val prepareBower = settingKey[Unit]("Prepare bower")
//
//prepareBower in compile := {
//  Process(Seq("bower", "install"), file("app/assets/frontend-up")).!
//}

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