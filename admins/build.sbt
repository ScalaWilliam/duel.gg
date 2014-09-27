name := "admins"

//lazy val root = ThisProject aggregate(scaladinAddon) dependsOn(scaladinAddon)

lazy val root = (project in file(".")) aggregate(scaladinAddon) dependsOn(scaladinAddon)

lazy val scaladinAddon = ProjectRef(uri("git://github.com/henrikerola/scaladin.git"), "addon")

scalaVersion := "2.11.2"

resolvers += "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"

resolvers += "vaadin-addons" at "http://maven.vaadin.com/vaadin-addons"

libraryDependencies += "com.orientechnologies" % "orientdb-community" % "1.7.8"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % Test

libraryDependencies += "us.woop.pinger" %% "pinger-data" % "1.0-SNAPSHOT"

libraryDependencies += "us.woop.pinger" %% "pinger-service" % "1.0-SNAPSHOT"

libraryDependencies += "com.hazelcast" % "hazelcast-client" % "3.2.5"

libraryDependencies += "com.vaadin" % "vaadin-push" % "7.3.0.rc1"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.2.2.v20140723"

libraryDependencies += "org.eclipse.jetty.websocket" % "javax-websocket-server-impl" % "9.2.2.v20140723"

libraryDependencies += "org.eclipse.jetty" % "jetty-servlet" % "9.2.2.v20140723"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "ch.qos.logback" % "log4j-bridge" % "0.9.7"

libraryDependencies += "org.slf4j" % "jul-to-slf4j" % "1.7.7"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "org.slf4j" % "jcl-over-slf4j" % "1.7.7"

libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.7"

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.2.v20140723"

libraryDependencies += "org.eclipse.jetty" % "jetty-continuation" % "9.2.2.v20140723"

libraryDependencies += "com.vaadin.addon" % "vaadin-charts" % "1.1.5"

libraryDependencies += "org.vaadin.addon" % "confirmdialog" % "2.0.5"

libraryDependencies += "com.vaadin" % "vaadin-themes" % "7.3.0.rc1"

libraryDependencies += "com.vaadin" % "vaadin-server" % "7.3.0.rc1"

libraryDependencies += "com.vaadin" % "vaadin-client-compiled" % "7.3.0.rc1"

libraryDependencies += "com.vaadin" % "vaadin-shared" %  "7.3.0.rc1"

libraryDependencies += "com.vaadin" % "vaadin-server" %  "7.3.0.rc1"

