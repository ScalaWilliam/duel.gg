import sbt._
trait Dependencies {
  val akkaVersion = "2.4.8"
  val scalactic = "org.scalactic" %% "scalactic" % "2.2.6"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion exclude("commons-logging", "commons-logging")
  val lz4 = "net.jpountz.lz4" % "lz4" % "1.3"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaAgent = "com.typesafe.akka" %% "akka-agent" % akkaVersion
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val scalaAsync = "org.scala-lang.modules" %% "scala-async" % "0.9.5"
  val jodaTime = "joda-time" % "joda-time" % "2.9.4"
  val jodaConvert = "org.joda" % "joda-convert" % "1.8.1"
  val commonsValidator = "commons-validator" % "commons-validator" % "1.5.1" exclude("commons-logging", "commons-logging")
  val gsCollections = "com.goldmansachs" % "gs-collections" % "7.0.3"
}
