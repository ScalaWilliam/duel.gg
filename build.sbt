lazy val pinger = project.settings(
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-actor" % "2.4.0",
    "com.typesafe.akka" %% "akka-agent" % "2.4.0",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.0" exclude ("commons-logging", "commons-logging"),
    "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % Test,
    "commons-validator" % "commons-validator" % "1.4.1" exclude ("commons-logging", "commons-logging"),
    "joda-time" % "joda-time" % "2.8.2",
    "org.joda" % "joda-convert" % "1.8.1",
    "org.json4s" %% "json4s-native" % "3.3.0",
    "org.scalactic" %% "scalactic" % "2.2.5",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  )
)

lazy val pingerService = (project in file("pinger-service")).enablePlugins(PlayScala).dependsOn(pinger)
.settings(libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-async" % "0.9.5",
  "com.h2database" % "h2" % "1.4.190"))

Seq(com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings :_*)

lazy val root = (project in file("."))
  .dependsOn(playersApi, playersCore, pinger, pingerService)
  .aggregate(playersApi, playersCore, pinger, pingerService)

lazy val playersApi = (project in file("players-api")).enablePlugins(PlayScala).dependsOn(playersCore)

lazy val playersCore = (project in file("players-core")).settings(
  libraryDependencies ++= Seq(
    "com.h2database" % "h2-mvstore" % "1.4.190",
    "com.typesafe.akka" %% "akka-agent" % "2.4.0",
    "org.scala-lang.modules" %% "scala-async" % "0.9.5"
  )
)

lazy val gamesCore = (project in file("games-core")).settings(
  libraryDependencies ++= Seq(
    "gcc" % "gcc" % "1.0.0-SNAPSHOT",
    json
  )
)

lazy val gamesApi = (project in file("games-api")).enablePlugins(PlayScala).dependsOn(gamesCore)
  .settings(
    routesImport += "binders._",
    libraryDependencies ++= Seq(
      ws,
      "com.typesafe.akka" %% "akka-actor" % "2.4.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.0",
      "com.typesafe.akka" %% "akka-agent" % "2.4.0",
      "org.scala-lang.modules" %% "scala-async" % "0.9.5"
    )
  )
