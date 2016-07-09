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
    scalactic, scalatest, json
  ))

lazy val gameEnricher = Project(
  id = "game-enricher",
  base = file("game-enricher"))
  .dependsOn(gameEvaluator)

lazy val pingerCore = Project(
  id = "pinger-core",
  base = file("pinger-core"))
  .settings(libraryDependencies ++= Seq(
    logback,
    akkaActor,
    akkaSlf4j,
    scalatest
  ))
  .dependsOn(pongParser, gameEvaluator)

lazy val pingerJournalReader = Project(
  id = "pinger-journal-reader",
  base = file("pinger-journal-reader")
).settings(libraryDependencies ++= Seq(
  scalaAsync,
  postgres,
  playSlick,
  akkaStream,
  json
))
  .dependsOn(pingerCore)

lazy val pingerService = Project(
  id = "pinger-service",
  base = file("pinger-service"))
  .enablePlugins(JavaServerAppPackaging)
  .dependsOn(pingerCore)
  .settings(libraryDependencies ++= Seq(
    scalaAsync,
    filters,
    akkaAgent
  ))
  .settings(
    includeGitStamp,
    dontDocument,
    name := "pinger-service",
    version := "5.0",
    mainClass := Some("gg.duel.pingerservice.PingerServiceApp")
  )

lazy val pongParser = Project(
  id = "pong-parser",
  base = file("pong-parser"))
  .settings(libraryDependencies ++= Seq(
    akkaActor,
    jodaTime,
    jodaConvert,
    commonsValidator,
    scalatest
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
    libraryDependencies += scalatest,
    libraryDependencies += akkaTestkit
  )

cancelable in Global := true

fork in run := true

