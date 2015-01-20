package gg.duel.pinger.service

import play.api.libs.ws._
import play.api.libs.ws.ning._
class StandaloneWSAPI(wsClientConfig: WSClientConfig = DefaultWSClientConfig()) extends WSAPI with java.io.Closeable {

  lazy val configuration = {
    new NingAsyncHttpClientConfigBuilder(wsClientConfig).build()
  }

  lazy val ningWsClient = {
    new NingWSClient(configuration)
  }

  override val client: WSClient = {
    ningWsClient
  }

  override def url(url: String): WSRequestHolder = {
    client.url(url)
  }

  def close(): Unit = {
    ningWsClient.close()
  }

}

