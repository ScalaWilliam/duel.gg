libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.typesafe.akka" %% "akka-agent" % "2.3.12",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.12" exclude ("commons-logging", "commons-logging"),
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12" % Test,
  "commons-validator" % "commons-validator" % "1.4.1" exclude ("commons-logging", "commons-logging"),
  "joda-time" % "joda-time" % "2.8.1",
  "org.joda" % "joda-convert" % "1.7",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.scalactic" %% "scalactic" % "2.2.5",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)
