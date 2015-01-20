enablePlugins(JavaAppPackaging, LinuxPlugin, UniversalPlugin)

organization := "gg.duel"

name := "pinger-service"

version := "2.0-SNAPSHOT"

scalaVersion := "2.11.5"

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
  "com.hazelcast" % "hazelcast" % "3.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "com.typesafe.akka" %% "akka-contrib" % "2.3.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.9" exclude ("commons-logging", "commons-logging"),
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9" % Test,
  "com.typesafe.akka" %% "akka-kernel" % "2.3.9",
  "commons-validator" % "commons-validator" % "1.4.0" exclude ("commons-logging", "commons-logging"),
  "io.spray" %% "spray-client" % "1.3.1",
  "joda-time" % "joda-time" % "2.4",
  "org.joda" % "joda-convert" % "1.7",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.scala-lang.modules" %% "scala-async" % "0.9.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "org.scalactic" %% "scalactic" % "2.2.1",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.basex" % "basex" % "8.0-20150120.080112-223" % "test" from "http://files.basex.org/maven/org/basex/basex/8.0-SNAPSHOT/basex-8.0-20150120.080112-223.jar",
  "org.basex" % "basex-api" % "basex-api-8.0-20150120.080412-216" % "test" from "http://files.basex.org/maven/org/basex/basex-api/8.0-SNAPSHOT/basex-api-8.0-20150120.080412-216.jar",
  "org.eclipse.jetty" % "jetty-servlet" % "8.1.16.v20140903" % "test",
  "org.eclipse.jetty" % "jetty-server" % "8.1.16.v20140903" % "test",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.16.v20140903" % "test",
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "test"
)

// Hazelcast likes for tests :-)
fork in Test := true

mainClass in Compile := Option("gg.duel.pinger.app.WootApp")

publishArtifact in (Compile, packageBin) := false

publishArtifact in (Universal, packageZipTarball) := true

publishArtifact in (Compile, packageDoc) := false

bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")

ideaExcludeFolders ++= Seq(".idea", ".idea_modules")

unmanagedClasspath in Runtime += baseDirectory.value / "src/universal/conf"

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)