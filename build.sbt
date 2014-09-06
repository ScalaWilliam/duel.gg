import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper._

organization := Common.groupId

scalaVersion := "2.11.2"

lazy val root = project.in( file(".") )
  .aggregate(admins, serviceApp, analytics, service, data, frontend)
  .dependsOn(admins, analytics, service, data, frontend, serviceApp)

lazy val analytics = project in file("pinger-analytics") dependsOn data

lazy val admins = project in file("admins")dependsOn(scaladinAddon)

lazy val service = project in file("pinger-service") dependsOn analytics

lazy val data = project in file("pinger-data")

lazy val scaladinAddon = ProjectRef(uri("git://github.com/henrikerola/scaladin.git"), "addon")

lazy val frontend = (project in file("frontend") enablePlugins PlayScala)

lazy val serviceApp = ((project in file("service-app")) dependsOn service)

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)

lazy val publishApps = taskKey[Unit]("Publish Frontend and ServiceApp")

publishApps := {
  val s: TaskStreams = streams.value
  (publish in (serviceApp, Universal)).value
  (publish in (frontend, Universal)).value
  s.log.info("Publishing complete")
}
