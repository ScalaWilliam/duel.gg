package lib

import com.maxmind.geoip.LookupService

object GeoLookup {

  lazy val lookupService = new LookupService("resources/GeoLiteCityv6.dat")

  def apply(ip: String): Option[(String, String)] = {
    for {
      location <- Option(lookupService.getLocationV6(ip))
      countryCode <- Option(location.countryCode)
      countryName <- Option(location.countryName)
    } yield (countryCode, countryName)
  }
}
