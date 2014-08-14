package us.woop.pinger.data.journal

import java.io.InputStream
import java.nio.{ByteBuffer, ByteOrder}

import akka.util.ByteStringBuilder

trait SauerBytesWriter {
   def write(sauerBytes: SauerBytes): Unit
   def close(): Unit
 }

object SauerBytesWriter {

   def createInjectedWriter(writeFn: Array[Byte] => Unit): SauerBytes => Unit =
   {sauerBytes =>

         val sauerBinary =
           SauerBytesBinary
             .toBytes(sauerBytes)
             .take(Short.MaxValue)

         implicit val byteOrdering = ByteOrder.BIG_ENDIAN

         val lengthBinary =
           new ByteStringBuilder()
             .putShort(sauerBinary.size)
             .result()
             .toArray

         writeFn(lengthBinary)
         writeFn(sauerBinary)
     }

   /**
    * Get some sauer bytes from a callback. Up to the caller to decide if they
    * want to retry at a different position.
    * Much nicer than using iterators here, imo.
    *
    * @param get - function from number of required bytes to a byte array
    * @return SauerBytes if we get any.
    */
   def readSauerBytes(get: Int => Option[Array[Byte]]): Option[SauerBytes] = {
     for {
       lengthBytes <- get(2)
       length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).getShort
       data <- get(length)
       if data.length == length
     } yield SauerBytesBinary.fromBytes(data)
   }

   def inputStreamNumBytes(inputStream: InputStream): Int => Option[Array[Byte]] = {
     num =>
       val byteBufferArray = ByteBuffer.allocate(num).array()
       val bytesRead = inputStream.read(byteBufferArray, 0, num)
       if ( bytesRead != -1 ) {
         Option(byteBufferArray)
       } else {
         None
       }
   }

   def iteratorNumBytes(iterator: Iterator[Byte]): Int => Option[Array[Byte]] = {
     num =>
       val array = iterator.take(num).toArray
       if ( array.size == num ) {
         Option(array)
       } else {
         None
       }
   }
  // maybe there's a simpler way to do this? I don't know
  def arrayNumBytes(list: Array[Byte]): Int => Option[Array[Byte]] = {
    var latestList = list
    (num: Int) => {
      val (start, end) = latestList.splitAt(num)
      latestList = end
      if ( start.size == num ) {
        Option(start)
      } else {
        None
      }
    }
  }

 }