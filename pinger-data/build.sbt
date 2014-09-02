organization := "us.woop.pinger"

name := "pinger-data"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

// for analyser
libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

// for ByteStrings
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.5"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

// for IP addresses
libraryDependencies += "commons-validator" % "commons-validator" % "1.4.0" exclude ("commons-logging", "commons-logging")

// for serialisation
libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.10"

// for making sure dates look good
libraryDependencies += "org.joda" % "joda-convert" % "1.7"

libraryDependencies += "joda-time" % "joda-time" % "2.4"