//logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0-M1")

addSbtPlugin("com.atlassian.labs" % "sbt-git-stamp" % "0.1.2")
