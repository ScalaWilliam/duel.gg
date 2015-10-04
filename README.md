# duel.gg open source

[![Join the chat at https://gitter.im/ScalaWilliam/duelgg](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ScalaWilliam/duelgg?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

I am developing the next generation of open-sources [duel.gg](http://duel.gg). It is currently all hosted underneath [alfa.duel.gg](http://alfa.duel.gg) with updates slowly going through to the mainline.

Build your own apps on top of this: all the public endpoints will be available for anyone to use and will be accessible via AJAX without cross-origin restrictions.

## Technology choices
* ClojureScript for front-end with Reagent: live reload and superior to JavaScript.
* Scala for back-end with Play: strong typing, rich ecosystem and robustness.

## players-api

The API is designed for combining user and clan management - so we can identify who has been playing as who throughout Sauerbraten's history.

Publicly accessible endpoints are:

|Endpoint | Function|
|-----|------|
| http://alfa.duel.gg/api/players/players/ | list all players |
| http://alfa.duel.gg/api/player/drakas/ | show player by ID |
| http://alfa.duel.gg/api/clans/ | list all clans |
| http://alfa.duel.gg/api/clan/woop/ | show clan by ID |

## games-api
It reads from a private game API and combines results with those of players-api.