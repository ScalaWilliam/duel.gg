

organization := Common.groupId

scalaVersion := "2.11.2"

lazy val root = project.in( file(".") )
  .aggregate(serviceApp, analytics, service, data, frontend)
  .dependsOn(analytics, service, data, frontend, serviceApp)

lazy val analytics = project in file("pinger-analytics") dependsOn data

lazy val service = project in file("pinger-service") dependsOn analytics

lazy val data = project in file("pinger-data")

lazy val frontend = (project in file("frontend") enablePlugins PlayScala)
//.settings(publishToSsh)

logLevel := Level.Warn

lazy val serviceApp = ((project in file("service-app")) dependsOn service)
//.settings(publishToSsh)

//lazy val publishToSsh =
//  publishTo := {
//    val keyFile = file(Path.userHome.absolutePath + "/.ssh/id_rsa")
//    Some(Resolver.ssh("wut", "prod-b.duel.gg") as("saule", keyFile))
//  }