import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper._

organization := Common.groupId

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

scalaVersion := "2.11.7"

lazy val root = project.in( file(".") )
  .aggregate(service, frontend, bobby, ns, nsf)
  .dependsOn(service, frontend, bobby, ns, nsf)

lazy val ns = project.dependsOn(service)

lazy val bobby = project in file("bobby")

lazy val service = project in file("pinger-service")

lazy val nsf = project.enablePlugins(PlayScala).dependsOn(ns)

lazy val frontend = (project in file("frontend") enablePlugins PlayScala)

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)

lazy val publishApps = taskKey[Unit]("Publish Frontend and ServiceApp")

publishApps := {
  val s: TaskStreams = streams.value
  (publish in (service, Universal)).value
  (publish in (frontend, Universal)).value
  (publish in (bobby, Universal)).value
  s.log.info("Publishing complete")
}
