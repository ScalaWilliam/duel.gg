package gg.duel.pingerservice

import java.io.InputStream

import play.api.libs.json.{JsObject, Json}

import scala.io.Codec

object GamesJournalReader {

  def fromInputStream(inputStream: InputStream): Iterator[(String, JsObject)] =
    scala.io.Source.fromInputStream(inputStream)(Codec.UTF8).getLines().collect {
      case ParseJsonLine(id, json) => id -> json
    }

  val parseLine = "([^\t]+)\t(.*)".r

  object ParseJsonLine {
    def unapply(string: String): Option[(String, JsObject)] = {
      PartialFunction.condOpt {
        PartialFunction.condOpt(string) {
          case parseLine(id, json) =>
            id -> Json.parse(json)
        }
      } {
        case Some((id, j: JsObject)) => id -> j
      }
    }
  }

}
