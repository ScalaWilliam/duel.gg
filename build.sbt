organization := Common.groupId

scalaVersion := "2.11.2"

lazy val root = project.in( file(".") ).aggregate(analytics, service, data, frontend).dependsOn(analytics, service, data, frontend)

lazy val analytics = project in file("pinger-analytics") dependsOn data

lazy val service = project in file("pinger-service") dependsOn analytics

lazy val data = project in file("pinger-data")

lazy val frontend = project in file("frontend") enablePlugins PlayScala

logLevel := Level.Warn