import sbt._
import Keys._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def projectSettings = Seq(
    scalaVersion := "2.11.7",
    organization := "gg.duel",
    version := "4.0-SNAPSHOT",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions",
      "-language:reflectiveCalls"
    ),
    resolvers += Resolver.mavenLocal
  )
  object autoImport {
    val includeGitStamp = com.atlassian.labs.gitstamp.GitStampPlugin.gitStampSettings
    val dontDocument = Seq(
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in packageDoc := false,
      sources in (Compile, doc) := Seq.empty
    )
  }
}
