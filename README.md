# duel.gg open source

[![Build Status](https://travis-ci.org/ScalaWilliam/duel.gg.svg?branch=master)](https://travis-ci.org/ScalaWilliam/duel.gg)

## Technology choices
* __Scala__ for back-end with Play: strong typing, rich ecosystem and robustness.
* __PHP__ for server-side frontend: dynamic typing, rapid prototyping, instant deployment.

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

## api

http://duel.gg/api/

# Compilation

```bash
$ sbt clean test dist
```
