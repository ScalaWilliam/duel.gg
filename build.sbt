lazy val root = (project in file(".")).dependsOn(api, core).aggregate(api, core)

lazy val api = project.enablePlugins(PlayScala).dependsOn(core).settings(
)

lazy val core = project.settings(
  libraryDependencies ++= Seq(
    "com.h2database" % "h2-mvstore" % "1.4.187",
    "com.typesafe.akka" %% "akka-agent" % "2.3.12",
    "org.scala-lang.modules" % "scala-async_2.11" % "0.9.5"
  )
)
