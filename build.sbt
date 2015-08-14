scalaVersion := "2.11.7"

libraryDependencies += "com.h2database" % "h2-mvstore" % "1.4.187"

libraryDependencies += "com.typesafe.akka" %% "akka-agent" % "2.3.12"

libraryDependencies += "org.scala-lang.modules" % "scala-async_2.11" % "0.9.5"

enablePlugins(PlayScala)

routesImport += "controllers.UserLookupBinders._"