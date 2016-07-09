package gg.duel.pingerservice

import java.io.OutputStream

import play.api.libs.json.JsObject

class GamesJournalWriter(outputStream: OutputStream) {
  def write(id: String, json: JsObject): Unit = {
    val theLine = s"$id\t$json\n"
    outputStream.write(theLine.getBytes("UTF-8"))
  }
}
