organization := "us.woop.pinger"

name := "pinger-service"

version := "1.1-SNAPSHOT"

scalaVersion := "2.11.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

libraryDependencies += "com.hazelcast" % "hazelcast" % "3.2.5"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.5"

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % "2.3.5"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3.5" exclude ("commons-logging", "commons-logging")

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.5" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-kernel" % "2.3.5"

// Hazelcast likes for tests :-)
fork in Test := true