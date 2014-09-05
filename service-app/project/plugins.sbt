resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.5-RC2")

addSbtPlugin("com.atlassian.labs" % "sbt-git-stamp" % "0.1.2")

// or:
//
//libraryDependencies += "joda-time" % "joda-time" % "2.4"
//
//libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "3.4.1.201406201815-r"