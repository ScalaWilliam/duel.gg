name := "duelgg"

lazy val root = Project(
  id = "duelgg",
  base = file("."))
  .dependsOn(
    pongParser,
    pingerCore,
    pingerService,
    gameEnricher,
    web,
    tests,
    masterserverClient,
    duelParser,
    pingerJournal
  ).aggregate(
  pongParser,
  pingerCore,
  pingerService,
  gameEnricher,
  web,
  tests,
  masterserverClient,
  duelParser,
  pingerJournal
)

lazy val gameEnricher = Project(
  id = "game-enricher",
  base = file("game-enricher"))
  .dependsOn(duelParser)

lazy val pingerCore = Project(
  id = "pinger-core",
  base = file("pinger-core"))
  .settings(libraryDependencies ++= Seq(
    logback,
    akkaActor,
    akkaSlf4j
  ))
  .dependsOn(pongParser)
  .dependsOn(duelParser)

lazy val pingerJournal = Project(
  id = "pinger-journal",
  base = file("pinger-journal")
)
  .dependsOn(pingerCore)

lazy val pingerService = Project(
  id = "pinger-service",
  base = file("pinger-service"))
  .enablePlugins(JavaServerAppPackaging)
  .dependsOn(pingerCore)
  .dependsOn(pingerJournal)
  .dependsOn(masterserverClient)
  .settings(libraryDependencies ++= Seq(
    scalaAsync,
    filters,
    akkaAgent
  ))
  .settings(
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
    commonsValidator
  ))

lazy val web = project
  .enablePlugins(PlayScala)
  .dependsOn(gameEnricher)
  .settings(version := "5.0")

lazy val tests = project
  .dependsOn(
    pongParser,
    pingerCore,
    pingerService,
    gameEnricher,
    masterserverClient,
    duelParser
  )
  .settings(
    libraryDependencies ++= Seq(
      scalatest,
      akkaTestkit
    ))

cancelable in Global := true

fork in Global in run := true

lazy val masterserverClient = Project(
  id = "masterserver-client",
  base = file("masterserver-client")
)

lazy val duelParser = Project(
  id = "duel-parser",
  base = file("duel-parser")
).dependsOn(pongParser)
  .settings(libraryDependencies ++= Seq(
    scalactic,
    json
  ))
