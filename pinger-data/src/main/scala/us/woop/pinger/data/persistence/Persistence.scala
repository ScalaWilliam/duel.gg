package us.woop.pinger.data.persistence

import java.io.File
import scala.util.control.NonFatal

object Persistence {
  import org.iq80.leveldb
  import leveldb._
  import org.fusesource.leveldbjni.JniDBFactory._
  val openAbsoluteDatabases = Memoize[File, DB]{ target =>
    try {
      val options = {
        val options = new Options()
        options.createIfMissing(true)
      }
      target.mkdirs()
      factory.open(target, options)
    } catch {
      case NonFatal(e) =>
        throw e
    }
  }
  def database(target: File) =
    openAbsoluteDatabases apply target.getCanonicalFile
}
