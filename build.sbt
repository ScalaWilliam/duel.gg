organization := Common.groupId

ideaExcludeFolders += ".idea"

ideaExcludeFolders += "cln-processor"

ideaExcludeFolders += ".idea_modules"

scalaVersion := "2.11.7"

lazy val root = project.in( file(".") )
  .aggregate(pinger, service)
  .dependsOn(pinger, service)

lazy val pinger = project

lazy val service = (project in file("pinger-service")).enablePlugins(PlayScala).dependsOn(pinger)

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)
