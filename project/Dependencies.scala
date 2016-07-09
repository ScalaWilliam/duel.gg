import sbt._
trait Dependencies {
  val scalactic = "org.scalactic" %% "scalactic" % "2.2.5"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % "test"
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.4.0" exclude("commons-logging", "commons-logging")
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.4.0"
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val scalaAsync = "org.scala-lang.modules" %% "scala-async" % "0.9.5"
  val postgres = "org.postgresql" % "postgresql" % "9.4-1204-jdbc42"
  val playSlick = "com.typesafe.play" %% "play-slick" % "1.1.0"
  val akkaStream = "com.typesafe.akka" %% "akka-stream-experimental" % "1.0"
  val h2 = "com.h2database" % "h2" % "1.4.190"
  val akkaAgent = "com.typesafe.akka" %% "akka-agent" % "2.4.0"
  val reactiveRabbit = "io.scalac" %% "reactive-rabbit" % "1.0.2"
  val jodaTime = "joda-time" % "joda-time" % "2.8.2"
  val jodaConvert = "org.joda" % "joda-convert" % "1.8.1"
  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.1" exclude("commons-logging", "commons-logging")
}
