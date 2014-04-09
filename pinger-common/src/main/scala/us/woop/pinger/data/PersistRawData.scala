package us.woop.pinger.data

object PersistRawData {
  class DatabaseUseException(e: Throwable) extends RuntimeException(s"Failed to use database because: $e", e)
}
