name := "duelgg"

lazy val root = Project(
  id = "duelgg",
  base = file("."))
  .dependsOn(
    gameEvaluator,
    pongParser,
    pingerCore,
    pingerService,
    gameEnricher,
    web,
    tests
  ).aggregate(
  gameEvaluator,
  pongParser,
  pingerCore,
  pingerService,
  gameEnricher,
  web,
  tests
)

lazy val gameEvaluator = Project(
  id = "game-evaluator",
  base = file("game-evaluator"))
  .dependsOn(pongParser)
  .settings(libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "org.scalactic" %% "scalactic" % "2.2.5",
    json
  ))

lazy val gameEnricher = Project(
  id = "game-enricher",
  base = file("game-enricher"))
  .dependsOn(gameEvaluator)

lazy val pingerCore = Project(
  id = "pinger-core",
  base = file("pinger-core"))
  .settings(libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-actor" % "2.4.0",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.0" exclude("commons-logging", "commons-logging"),
    "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % "test",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  ))
  .dependsOn(pongParser, gameEvaluator)

lazy val pingerJournalReader = Project(
  id = "pinger-journal-reader",
  base = file("pinger-journal-reader")
).settings(libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-async" % "0.9.5",
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
  "com.typesafe.play" %% "play-slick" % "1.1.0",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
  json
))
  .dependsOn(pingerCore)

lazy val pingerService = Project(
  id = "pinger-service",
  base = file("pinger-service"))
  .enablePlugins(PlayScala)
  .dependsOn(pingerCore)
  .settings(name := "pingerservice")
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-async" % "0.9.5",
    "com.h2database" % "h2" % "1.4.190",
    filters,
    "com.typesafe.akka" %% "akka-agent" % "2.4.0",
    "io.scalac" %% "reactive-rabbit" % "1.0.2",
    "com.typesafe.akka" %% "akka-stream-experimental" % "1.0"
  ))
  .settings(
    includeGitStamp,
    dontDocument
  )

lazy val pongParser = Project(
  id = "pong-parser",
  base = file("pong-parser"))
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.0",
    "joda-time" % "joda-time" % "2.8.2",
    "org.joda" % "joda-convert" % "1.8.1",
    "commons-validator" % "commons-validator" % "1.4.1" exclude("commons-logging", "commons-logging"),
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  ))

lazy val web = project
  .enablePlugins(PlayScala)
  .settings(
    version := "5.0"
  )

lazy val tests = project
  .dependsOn(
    gameEvaluator,
    pongParser,
    pingerCore,
    pingerService,
    gameEnricher
  )
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  )
