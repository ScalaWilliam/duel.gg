import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.SettingsHelper._

scalaVersion := "2.11.2"

libraryDependencies += "us.woop.pinger" %% "pinger-service" % "1.0-SNAPSHOT"

packageArchetype.java_application

name := "pinger-app"

mapGenericFilesToLinux

publishTo := {
  val keyFile = file(Path.userHome.absolutePath + "/.ssh/id_rsa")
  Some(Resolver.ssh("wut", Some("prod-b.duel.gg"), None, Some("published")) as("saule", keyFile))
}

publishArtifact in (Compile, packageBin) := false

publishArtifact in (Universal, packageZipTarball) := true

makeDeploymentSettings(Universal, packageZipTarball in Universal, "tgz")