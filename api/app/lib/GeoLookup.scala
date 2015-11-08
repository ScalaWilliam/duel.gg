package lib

import java.io.File

import com.maxmind.geoip.LookupService

object GeoLookup {

  lazy val lookupService = {
    val A = new File("resources/GeoLiteCityv6.dat")
    val B = new File("api/resources/GeoLiteCityv6.dat")
    val C = new File("../resources/GeoLiteCityv6.dat")
    val file = if ( B.exists() ) B else if ( C.exists()) C else A
    new LookupService(file)
  }

  def apply(ip: String): Option[(String, String)] = {
    for {
      location <- Option(lookupService.getLocationV6(ip))
      countryCode <- Option(location.countryCode)
      countryName <- Option(location.countryName)
    } yield (countryCode, countryName)
  }
}
