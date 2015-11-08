
lazy val geoIpFiles = taskKey[List[File]]("Files for GeoIp")
lazy val downloadGeoIpFiles = taskKey[Unit]("Files for GeoIp")

run <<= (run in Runtime) dependsOn(downloadGeoIpFiles)
(test in Test) <<= (test in Test) dependsOn(downloadGeoIpFiles)

downloadGeoIpFiles := geoIpFiles.value

geoIpFiles := {
  import sbt._
  import IO._
  val resourcesDirectory = baseDirectory.value / "resources"
  if ( !resourcesDirectory.exists() ) {
    createDirectory(resourcesDirectory)
  }
  val cityFileGz = resourcesDirectory / "GeoLiteCityv6.dat.gz"
  val cityFile = resourcesDirectory / "GeoLiteCityv6.dat"
  val ipFileGz = resourcesDirectory / "GeoIPASNumv6.dat.gz"
  val ipFile = resourcesDirectory / "GeoIPASNumv6.dat"
  if (!cityFile.exists()) {
    download(url("http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6-beta/GeoLiteCityv6.dat.gz"), cityFileGz)
    gunzip(cityFileGz, cityFile)
    delete(cityFileGz)
  }
  if (!ipFile.exists()) {
    download(url("http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNumv6.dat.gz"), ipFileGz)
    gunzip(ipFileGz, ipFile)
    delete(ipFileGz)
  }
  List(cityFile, ipFile)
}

mappings in Universal ++= {
  geoIpFiles.value.map{ f =>
    f -> s"resources/${f.getName}"
  }
}
