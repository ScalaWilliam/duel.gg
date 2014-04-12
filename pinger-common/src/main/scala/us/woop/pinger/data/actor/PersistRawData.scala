package us.woop.pinger.data.actor

object PersistRawData {
  class DatabaseUseException(e: Throwable) extends RuntimeException(s"Failed to use database because: $e", e)
}
