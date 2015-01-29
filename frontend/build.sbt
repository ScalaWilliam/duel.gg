import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper._
organization := "gg.duel"
name := "frontend"
version := "1.0-SNAPSHOT"
enablePlugins(PlayScala, SbtWeb)
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
scalaVersion := "2.11.5"
libraryDependencies ++= Seq(
  "com.hazelcast"%"hazelcast"%"3.4",
  "org.scalactic" %%"scalactic"%"2.2.1",
  "com.maxmind.geoip2"%"geoip2"%"2.1.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.2",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "org.joda" % "joda-convert" % "1.7",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "commons-net" % "commons-net" % "3.3",
  ws
)
sources in (Compile,doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false
includeFilter in (Assets, LessKeys.less) := "*.less"
excludeFilter in (Assets, LessKeys.less) := "_*.less"
crossPaths := false
publishArtifact in (Compile, packageBin) := false
publishArtifact in (Universal, packageZipTarball) := true
makeDeploymentSettings(Universal, packageZipTarball in Universal, "tgz")