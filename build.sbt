name := "duelgg"

lazy val root = (project in file("."))
  .dependsOn(gameEvaluator, pongParser, api, pingerCore, pingerService, playersCore, gamesCore)
  .aggregate(gameEvaluator, pongParser, api, pingerCore, pingerService, playersCore, gamesCore)

lazy val gameEvaluator = (project in file("game-evaluator"))
  .dependsOn(pongParser)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % Test,
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  ))

lazy val pongParser = (project in file("pong-parser"))
  .settings(libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-actor" % "2.4.0",
    "com.typesafe.akka" %% "akka-agent" % "2.4.0",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.0" exclude("commons-logging", "commons-logging"),
    "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % Test,
    "commons-validator" % "commons-validator" % "1.4.1" exclude("commons-logging", "commons-logging"),
    "joda-time" % "joda-time" % "2.8.2",
    "org.joda" % "joda-convert" % "1.8.1",
    "org.json4s" %% "json4s-native" % "3.3.0",
    "org.scalactic" %% "scalactic" % "2.2.5",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  ))

lazy val api = (project in file("api")).enablePlugins(PlayScala).dependsOn(gamesCore, playersCore)
  .settings(
    routesImport += "binders._",
    resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      ws,
      "com.typesafe.akka" %% "akka-actor" % "2.4.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.0",
      "com.typesafe.akka" %% "akka-agent" % "2.4.0",
      "org.scala-lang.modules" %% "scala-async" % "0.9.5",
      "de.heikoseeberger" %% "akka-sse" % "1.1.0",
      "org.scalatest" %% "scalatest" % "2.2.5" % "test"
    )).settings(includeGitStamp).dependsOn(pingerCore % "test->test").settings(dontDocument)

lazy val pingerCore = (project in file("pinger-core")).settings(
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-actor" % "2.4.0",
    "com.typesafe.akka" %% "akka-agent" % "2.4.0",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.0" exclude("commons-logging", "commons-logging"),
    "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % Test,
    "commons-validator" % "commons-validator" % "1.4.1" exclude("commons-logging", "commons-logging"),
    "joda-time" % "joda-time" % "2.8.2",
    "org.joda" % "joda-convert" % "1.8.1",
    "org.json4s" %% "json4s-native" % "3.3.0",
    "org.scalactic" %% "scalactic" % "2.2.5",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  )
).dependsOn(pongParser, gameEvaluator)

lazy val pingerService = (project in file("pinger-service")).enablePlugins(PlayScala).dependsOn(pingerCore)
  .settings(libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-async" % "0.9.5",
    "com.h2database" % "h2" % "1.4.190", filters)).settings(includeGitStamp).settings(dontDocument)

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
