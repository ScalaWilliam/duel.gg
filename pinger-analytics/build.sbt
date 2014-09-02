organization := "us.woop.pinger"

name := "pinger-analytics"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.3.4" exclude ("commons-logging", "commons-logging") exclude ("com.typesafe.play","build-link")

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.10"

libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.1"

libraryDependencies += "com.hazelcast" % "hazelcast" % "3.2.5"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.5"

libraryDependencies += "us.woop.pinger" %% "pinger-data" % "1.0-SNAPSHOT"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"

libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.7"
