lazy val root = (project in file(".")).dependsOn(playersApi, playersCore).aggregate(playersApi, playersCore)

lazy val playersApi = (project in file("players-api")).enablePlugins(PlayScala).dependsOn(playersCore)

lazy val playersCore = (project in file("players-core")).settings(
  libraryDependencies ++= Seq(
    "com.h2database" % "h2-mvstore" % "1.4.189",
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
