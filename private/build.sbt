organization := Common.groupId

ideaExcludeFolders += ".idea"

ideaExcludeFolders += "cln-processor"

ideaExcludeFolders += ".idea_modules"

scalaVersion := "2.11.7"

lazy val root = project.in( file(".") )
  .aggregate(pinger, pingerService)
  .dependsOn(pinger, pingerService)

lazy val pinger = project

lazy val pingerService = (project in file("pinger-service")).enablePlugins(PlayScala).dependsOn(pinger)

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)
