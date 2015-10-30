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

### games api
|Endpoint | Function|
|-----|------|
| [/new-games/](http://api.duel.gg/new-games/) | __EventSource / Server-Sent Events__ - automatic push of new games coming through.|
| [/games/duel/first/?player=vaQ%27Frosty&limit=2](http://api.duel.gg/games/duel/first/?player=vaQ%27Frosty&limit=2) | find first duel games involving Drakas. Get two. |
| [/games/all/recent/?player=vaQ%27Frosty&player=w00p%27raffael&operator=and](http://api.duel.gg/games/all/recent/?player=vaQ%27Frosty&player=w00p%27raffael&operator=and) | Find recent games with both raffael and Frosty |
| [/games/ctf/recent/?clan=rb&clan=woop&operator=or](http://api.duel.gg/games/ctf/recent/?clan=rb&clan=woop&operator=or) | Find recent games with both raffael and Frosty |
| [/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%27raffael](http://api.duel.gg/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%27raffael) | Find raffael's ctf game at the specified time, the game before and the game after |
| [/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%27raffael&radius=15](http://api.duel.gg/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%27raffael) | Find raffael's ctf game at the specified time, 15 games before, 15 after |
| [/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%27raffael&previous=0&next=30](http://api.duel.gg/games/ctf/focus/2015-09-11T22:57:05Z/?player=w00p%27raffael) | Find raffael's ctf game at the specified time, 0 games before, 30 games after |
| [/game/2015-08-11T20:47:11Z/](http://api.duel.gg/game/2015-08-11T20:47:11Z/) | game at a specific ID |
| [/games/?game=2015-08-11T20:47:11Z&game=2015-08-11T20:58:29Z](http://api.duel.gg/games/?game=2015-08-11T20:47:11Z&game=2015-08-11T20:58:29Z) | games with those start times |

### players api
|Endpoint | Function|
|-----|------|
| [/players/](http://api.duel.gg/players/) | list all players |
| [/player/drakas/](http://api.duel.gg/player/drakas/) | show player by ID |
| [/clans/](http://api.duel.gg/clans/) | list all clans |
| [/woop/](http://api.duel.gg/clan/woop/) | show clan by ID |

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
