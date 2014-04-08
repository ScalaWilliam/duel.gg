package us.woop.pinger.client.data

object PersistRawData {
  class DatabaseUseException(e: Throwable) extends RuntimeException(s"Failed to use database because: $e", e)
}
