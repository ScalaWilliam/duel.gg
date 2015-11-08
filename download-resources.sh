#!/usr/bin/env bash
mkdir -p resources
[ -e GeoLiteCityv6.dat ] || { wget -O - http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6-beta/GeoLiteCityv6.dat.gz | gunzip - > resources/GeoLiteCityv6.dat; }
[ -e GeoIPASNumv6.dat ] || { wget -O - http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6-beta/GeoIPASNumv6.dat.gz | gunzip - > resources/GeoIPASNumv6.dat; }