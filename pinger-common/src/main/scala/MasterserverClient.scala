import java.io.{BufferedReader, InputStreamReader, DataOutputStream}
import java.net.Socket
import scala.util.control.NonFatal
import scala.util.Try

/** 01/02/14 */
object MasterserverClient {

  val serverRegex = """^addserver (\d+\.\d+\.\d+\.\d+) (\d+)$""".r
  val parseServerLine: PartialFunction[String, (String, Int)] =
    { case serverRegex(host, port) => (host, port.toInt) }

  val sauerMasterserver = ("sauerbraten.org", 28787)

  def getServers(masterServer: (String, Int)): Set[(String, Int)] = {
    val socket = new Socket(masterServer._1, masterServer._2)
    try {
      val dataOutputStream = new DataOutputStream(socket.getOutputStream)
      try {
        val inputStreamReader = new InputStreamReader(socket.getInputStream)
        try {
          val dataInputStream = new BufferedReader(inputStreamReader)
          try {
            dataOutputStream.writeBytes("list\n")
            def getLine = Try(Option(dataInputStream.readLine)).toOption.flatten.filterNot{_ == '\0'.toString}
            val contents = Stream.continually(getLine).takeWhile(_ != None).flatten.toList
            try {
              (contents map parseServerLine).toSet
            } catch {
              case NonFatal(e) => throw new MatchError(s"Failed to parse contents due to '$e' - contents: '$contents'")
            }
          } finally {
            dataInputStream.close()
          }
        } finally {
          inputStreamReader.close()
        }
      } finally {
        dataOutputStream.close()
      }
    } finally {
      socket.close()
    }
  }

}
