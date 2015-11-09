name := "duelgg"

lazy val root = Project(
    id = "duelgg",
    base = file("."))
  .dependsOn(
    gameEvaluator,
    pongParser,
    api,
    pingerCore,
    pingerService,
    gamesQuery,
    gameEnricher
  ).aggregate(
  gameEvaluator,
  pongParser,
  api,
  pingerCore,
  pingerService,
  gamesQuery,
gameEnricher
)

lazy val api = Project(
    id = "api",
    base = file("api"))
  .enablePlugins(PlayScala)
  .dependsOn(gamesQuery, gameEnricher)
  .settings(
    routesImport ++= Seq("binders._", "gg.duel.query._"),
    resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      ws,
      "com.typesafe.akka" %% "akka-actor" % "2.4.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.0",
      "com.typesafe.akka" %% "akka-agent" % "2.4.0",
      "org.scala-lang.modules" %% "scala-async" % "0.9.5",
      "de.heikoseeberger" %% "akka-sse" % "1.1.0",
      "org.scalatest" %% "scalatest" % "2.2.5" % "test",
//      "com.typesafe.play" %% "play-slick" % "1.1.0",
//      "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
      filters,
      "org.apache.httpcomponents" % "fluent-hc" % "4.5.1",
      "org.scalatestplus" %% "play" % "1.4.0-M4" % "test",
      "org.jsoup" % "jsoup" % "1.8.3",
      "mysql" % "mysql-connector-java" % "5.1.37",
      "io.scalac" %% "reactive-rabbit" % "1.0.2",
      "com.maxmind.geoip" % "geoip-api" % "1.2.14"
))
  .settings(includeGitStamp, dontDocument)
  .dependsOn(gameEvaluator % "test->test")

lazy val gameEvaluator = Project(
    id = "game-evaluator",
    base = file("game-evaluator"))
  .dependsOn(pongParser)
  .settings(libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "org.scalactic" %% "scalactic" % "2.2.5",
    json
  ))

lazy val gamesQuery = Project(
    id = "games-query",
    base = file("games-query"))
  .settings(
    libraryDependencies += json
  )

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
