lazy val root = (project in file(".")).dependsOn(playersApi, playersCore).aggregate(playersApi, playersCore)

lazy val playersApi = (project in file("players-api")).enablePlugins(PlayScala).dependsOn(playersCore)

lazy val playersCore = (project in file("players-core")).settings(
  libraryDependencies ++= Seq(
    "com.h2database" % "h2-mvstore" % "1.4.187",
    "com.typesafe.akka" %% "akka-agent" % "2.3.12",
    "org.scala-lang.modules" % "scala-async_2.11" % "0.9.5"
  )
)
