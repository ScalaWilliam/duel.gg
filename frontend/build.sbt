name := "duelgg-frontend"

version := "1.0.0-SNAPSHOT"

libraryDependencies += "com.hazelcast" % "hazelcast-client" % "3.2.3"

libraryDependencies += "us.woop.pinger" % "pinger-analytics" % "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

resolvers += Resolver.mavenLocal

libraryDependencies += "net.xqj" % "xqj-connection-pool" % "0.9.0-SNAPSHOT"

libraryDependencies += "org.apache.commons" % "commons-pool2" % "2.2"

libraryDependencies += ws