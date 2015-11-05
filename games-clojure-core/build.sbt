lazy val buildClojure = taskKey[String]("Compile Clojure")
lazy val clojureNamespaces = SettingKey[List[String]]("Clojure namespaces to compile")
lazy val clojureTarget = SettingKey[File]("Clojure target")

resolvers += "clojars" at "http://clojars.org/repo"

libraryDependencies ++= Seq(
    "org.clojure" % "clojure" % "1.7.0",
    "org.clojure" % "data.json" % "0.2.6",
    "de.bertschneider" % "clj-geoip" % "0.2",
    "clj-time" % "clj-time" % "0.10.0"
)

clojureTarget := baseDirectory.value / "target" / "clojure" / "classes"

clojureNamespaces := List("gcc.game", "gcc.enrichment")

unmanagedResourceDirectories in Compile += clojureTarget.value

buildClojure := {
	sbt.IO.createDirectory(clojureTarget.value)
	val srcPath = (baseDirectory.value / "src").getCanonicalPath
	val dstPath = clojureTarget.value.getCanonicalPath
	val depJars = (dependencyClasspath in Compile).value.map(_.data).map(_.getCanonicalPath)
	val fullCp = List(srcPath, dstPath) ++ depJars
	val theCallN = List("java", "-Dclojure.compile.path=" + clojureTarget.value.getCanonicalPath, "-cp", fullCp.mkString(":"), "clojure.lang.Compile") ++ clojureNamespaces.value
  val logger = streams.value.log
	Process(theCallN) ! logger
	"K"
}