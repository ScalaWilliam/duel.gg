# duel.gg open source

## Technology choices
* __Scala__ for back-end with Play: strong typing, rich ecosystem and robustness.
* __Clojure__ for data transformations: dynamic, low resistance map traversal.
* __ClojureScript__ for front-end with Reagent: live reload and superior to JavaScript.

## Aims, objectives, expectations

I realised there is simply too much work for me to take on. I would like to see people use the duel.gg APIs for ad-hoc projects which would be easy to build, such as:

* "Drakas's recent games" embed built with pure JavaScript.
* "Woop's recent games" embed built with pure JavaScript.
* "Woop's recent duels" embed built with pure JavaScript.
* ELO leagues
* Duel leagues
* CTF leagues
* Clan leagues

Build your own apps on top of this: all the public endpoints will be available for anyone to use and will be accessible via AJAX without cross-origin restrictions.

## Packages

* __api__: the main public API.
* __games-clojure-core__: the enrichment of game JSONs.
* __pinger-core__: ping and process responses from Sauerbraten servers.
* __pinger-service__: persist captured games permanently in a DB. Internal.
* __players-core__: player management logic.

## api

This is the core external API that everybody will consume. Following endpoints are available:

|Endpoint | Function|
|-----|------|
| http://alfa.duel.gg/api/games/recent/ | recent games |
| http://alfa.duel.gg/api/games/first/ | first games |
| http://alfa.duel.gg/api/games/until/2015-10-24T18:37:40Z/ | games started until that time |
| http://alfa.duel.gg/api/games/to/2015-10-24T18:37:40Z/ | games started before that time |
| http://alfa.duel.gg/api/games/from/2015-10-24T18:37:40Z/ | games started from that time |
| http://alfa.duel.gg/api/games/after/2015-10-24T18:37:40Z/ | games started after that time |
| http://alfa.duel.gg/api/ctf/games/recent/ | ctf games started after that time |
| http://alfa.duel.gg/api/ctf/games/recent/?player=w00p|Drakas | recent CTF games with Drakas in them |
| http://alfa.duel.gg/api/duels/recent/?player=w00p|Drakas&player=w00p|raffael&clan=RB | recent CTF games with either: Drakas, raffael, or RB in them |
| http://alfa.duel.gg/api/game/2015-08-11T20:47:11Z/ | game at a specific ID |
| http://alfa.duel.gg/api/games/?game=2015-08-11T20:47:11Z&game=2015-08-11T20:58:29Z | games with those start times |
| http://alfa.duel.gg/api/players/ | list all players |
| http://alfa.duel.gg/api/player/drakas/ | show player by ID |
| http://alfa.duel.gg/api/clans/ | list all clans |
| http://alfa.duel.gg/api/clan/woop/ | show clan by ID |

### E-mails

Player Google e-mail addresses will be public. If you don't wish your private e-mail to be seen, simply create a new Google account just for Sauerbraten.

There are two use cases for public e-mail addresses:
* Users will be able to e-mail eachother without relying on our own systems - Google takes care of all this.
* Developers will be able to build applications on top of this API - getting a user's ID will be very simple after the user signs in via Google sign in.

# Compilation

First download GeoIP data into the 'resources' directory: https://github.com/bertschneider/clj-geoip/blob/master/scripts/UpdateGeoIpFiles.sh

Then Install the Clojure project and compile the Scala:
```bash
$ cd games-clojure-core
$ lein with-profile precomp install
$ cd ..
$ sbt dist
```
