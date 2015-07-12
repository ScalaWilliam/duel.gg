//enablePlugins(JavaAppPackaging, LinuxPlugin, UniversalPlugin)

organization := "gg.duel"

name := "pinger-service"

version := "2.0-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
  "com.hazelcast" % "hazelcast" % "3.4.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.typesafe.akka" %% "akka-agent" % "2.3.12",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "com.typesafe.akka" %% "akka-contrib" % "2.3.12",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.12" exclude ("commons-logging", "commons-logging"),
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12" % Test,
  "com.typesafe.akka" %% "akka-kernel" % "2.3.12",
  "commons-validator" % "commons-validator" % "1.4.1" exclude ("commons-logging", "commons-logging"),
  "io.spray" %% "spray-client" % "1.3.3",
  "joda-time" % "joda-time" % "2.8.1",
  "org.joda" % "joda-convert" % "1.7",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.scala-lang.modules" %% "scala-async" % "0.9.4",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "org.scalactic" %% "scalactic" % "2.2.5",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "org.basex" % "basex" % "8.0" % "test" from "http://files.basex.org/maven/org/basex/basex/8.0/basex-8.0.jar",
  "org.basex" % "basex-api" % "basex-api-8.0" % "test" from "http://files.basex.org/maven/org/basex/basex-api/8.0/basex-api-8.0.jar",
  "org.eclipse.jetty" % "jetty-servlet" % "9.3.0.v20150612" % "test",
  "org.eclipse.jetty" % "jetty-server" % "9.3.0.v20150612" % "test",
  "org.eclipse.jetty" % "jetty-webapp" % "9.3.0.v20150612" % "test",
  "com.maxmind.geoip2"%"geoip2"%"2.3.1",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "test"
)

// Hazelcast likes for tests :-)
fork in Test := true

mainClass in Compile := Option("gg.duel.pinger.app.WootApp")

publishArtifact in (Compile, packageBin) := false

//publishArtifact in (Universal, packageZipTarball) := true

publishArtifact in (Compile, packageDoc) := false

//bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")

//ideaExcludeFolders ++= Seq(".idea", ".idea_modules")

//unmanagedClasspath in Runtime += baseDirectory.value / "src/universal/conf"

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)