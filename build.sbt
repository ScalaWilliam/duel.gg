organization := Common.groupId

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

scalaVersion := "2.11.7"

lazy val root = project.in( file(".") )
  .aggregate(service, nsf)
  .dependsOn(service, nsf)

lazy val service = project in file("pinger-service")

lazy val nsf = project.enablePlugins(PlayScala).dependsOn(service)

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)
