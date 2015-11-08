#!/bin/bash
mkdir -p resources
cd resources/
[ -e GeoLiteCityv6.dat ] || { wget -O - http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6-beta/GeoLiteCityv6.dat.gz | gunzip - > GeoLiteCityv6.dat; }
[ -e GeoIPASNumv6.dat ] || { wget -O - http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNumv6.dat.gz | gunzip - > GeoIPASNumv6.dat; }
ls -lsa
ls -lsa ..