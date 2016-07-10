import sbt._
import Keys._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def projectSettings = Seq(
    scalaVersion := "2.11.8",
    organization := "gg.duel",
    version := "5.0-SNAPSHOT",
    updateOptions := updateOptions.value.withCachedResolution(true),
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions",
      "-language:reflectiveCalls"
    ),
    resolvers += Resolver.mavenLocal
  )
  object autoImport extends Dependencies {
    val dontDocument = Seq(
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in packageDoc := false,
      sources in (Compile, doc) := Seq.empty
    )
  }
}
