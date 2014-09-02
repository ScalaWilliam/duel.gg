//import play.Project._

name := "duelgg-frontend"

version := "1.0.0-SNAPSHOT"

libraryDependencies += "com.hazelcast" % "hazelcast-client" % "3.2.5"

//playScalaSettings

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies += "com.github.scala-incubator.io" % "scala-io-core_2.11" % "0.4.3-1"

libraryDependencies += "com.github.scala-incubator.io" % "scala-io-file_2.11" % "0.4.3-1"

libraryDependencies += "com.hazelcast" % "hazelcast" % "3.2.5"

libraryDependencies += ws

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"