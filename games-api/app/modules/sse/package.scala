package modules

import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.scaladsl.Flow

import scala.concurrent.Future

package object sse {

  type HttpConnection = Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]]

}
