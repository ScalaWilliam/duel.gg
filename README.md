# duel.gg open source

[![Join the chat at https://gitter.im/ScalaWilliam/duelgg](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ScalaWilliam/duelgg?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

I am developing the next generation of open-sources [duel.gg](http://duel.gg). It is currently all hosted underneath [alfa.duel.gg](http://alfa.duel.gg) with updates slowly going through to the mainline.

Build your own apps on top of this: all the public endpoints will be available for anyone to use and will be accessible via AJAX without cross-origin restrictions.

## Technology choices
* ClojureScript for front-end with Reagent: live reload and superior to JavaScript.
* Scala for back-end with Play: strong typing, rich ecosystem and robustness.

## Aims, objectives, expectations
I realised there is simply too much work for me to take on. I would like to see people use the duel.gg APIs for ad-hoc projects which would be easy to build, such as:

* "Drakas's recent games" embed built with pure JavaScript.
* "Woop's recent games" embed built with pure JavaScript.
* "Woop's recent duels" embed built with pure JavaScript.
* ELO leagues
* Duel leagues
* CTF leagues
* Clan leagues

## players-api

The API is designed for combining user and clan management - so we can identify who has been playing as who throughout Sauerbraten's history.

### E-mails

Player Google e-mail addresses are public. If you don't wish your private e-mail to be seen, simply create a new Google account just for Sauerbraten.

There are two use cases for public e-mail addresses:
* Users will be able to e-mail eachother without relying on our own systems - Google takes care of all this.
* Developers will be able to build applications on top of this API - getting a user's ID will be very simple after the user signs in via Google sign in.

### Endpoints

Publicly accessible endpoints are:

|Endpoint | Function|
|-----|------|
| http://alfa.duel.gg/api/players/players/ | list all players |
| http://alfa.duel.gg/api/players/player/drakas/ | show player by ID |
| http://alfa.duel.gg/api/players/clans/ | list all clans |
| http://alfa.duel.gg/api/players/clan/woop/ | show clan by ID |

## games-api
It reads from a private game API and combines results with those of players-api.

# Compilation

First download GeoIP data into the 'resources' directory: https://github.com/bertschneider/clj-geoip/blob/master/scripts/UpdateGeoIpFiles.sh

Then Install the Clojure project and compile the Scala:
```bash
$ cd games-clojure-core
$ lein with-profile precomp install
$ cd ..
$ sbt dist
```

