organization := Common.groupId

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

scalaVersion := "2.11.7"

lazy val root = project.in( file(".") )
  .aggregate(service, ns, nsf)
  .dependsOn(service, ns, nsf)

lazy val ns = project.dependsOn(service)

lazy val service = project in file("pinger-service")

lazy val nsf = project.enablePlugins(PlayScala).dependsOn(ns)

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)
