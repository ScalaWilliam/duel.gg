package gg.duel.pinger.masterserver

import java.io.{BufferedReader, DataOutputStream, InputStreamReader}
import java.net.Socket

import scala.util.Try

case class MasterserverClient(host: String, port: Int) {

  private type Closes = { def close(): Unit }
  private def using[T <: Closes, V](e: => T)(f: T => V): V = {
    val v = e
    try f(e)
    finally v.close()
  }

  def getServers: Set[(String, Int)] = {
    using(new Socket(host, port)) { socket =>
      using(new DataOutputStream(socket.getOutputStream)) { dataOutputStream =>
        using(new InputStreamReader(socket.getInputStream)) { inputStreamReader =>
          using(new BufferedReader(inputStreamReader)) { dataInputStream =>
            dataOutputStream.writeBytes("list\n")
            def getLine = Try(Option(dataInputStream.readLine)).toOption.flatten.filterNot {
              _ == '\u0000'.toString
            }
            MasterserverClient.Parser(
              lines = Stream.continually(getLine).takeWhile(_.nonEmpty).flatten.toList
            ).getServers
          }
        }
      }
    }
  }

}


object MasterserverClient {

  case class Parser(lines: List[String]) {
    private val serverRegex = """^addserver (\d+\.\d+\.\d+\.\d+) (\d+)$""".r
    private val parseServerLine: PartialFunction[String, (String, Int)] = {
      case serverRegex(h, p) => (h, p.toInt)
    }
    def getServers: Set[(String, Int)] = {
      lines.map(parseServerLine.lift).flatten.toSet
    }
  }
  def default = MasterserverClient(
    host = "sauerbraten.org",
    port = 28787
  )
}