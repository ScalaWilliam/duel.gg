package us.woop.pinger

import org.scalatest.{Matchers, WordSpec}
import io.netty.channel.socket.SocketChannel
import io.netty.channel._
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.{Unpooled, ByteBuf}
import java.net.ConnectException
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MasterserverClientTest extends WordSpec with Matchers {

  "Masterserver client" must {
    "fail when it's not possible to conenct to the masterserver" in {
      a [ConnectException] shouldBe thrownBy {
        MasterserverClient.getServers(("127.0.0.1", 9219))
      }
    }
    "list no servers with an empty response" in {
      val server = sauerMasterserver(Nil)
      MasterserverClient.getServers(("127.0.0.1", 9919)) shouldBe empty
      server._2.channel().close().sync()
    }
    "throw a MatchError when the response is bad" in {
      val server = sauerMasterserver("hello chaps!"::Nil)
      a [MatchError] shouldBe thrownBy {
        MasterserverClient.getServers(("127.0.0.1", 9919))
      }
      server._2.channel().close().sync()
    }
    "list one server" in {
      val server = sauerMasterserver("addserver 123.41.13.24 1234"::Nil)
      val servers = MasterserverClient.getServers(("127.0.0.1", 9919))
      server._2.channel().close().sync()
      servers should contain ("123.41.13.24" -> 1234)
    }
    "list several servers" in {
      val server = sauerMasterserver("addserver 123.41.13.24 1234\naddserver 123.41.13.24 1235"::Nil)
      val servers = MasterserverClient.getServers(("127.0.0.1", 9919))
      server._2.channel().close().sync()
      servers should contain only ("123.41.13.24" -> 1234, "123.41.13.24" -> 1235)
    }
  }

  def sauerMasterserver(respondingWith: List[String]) = {
    val bossGroup = new NioEventLoopGroup()
    val workerGroup = new NioEventLoopGroup()
    val bootstrap = new ServerBootstrap()
    val g = bootstrap.group(bossGroup, workerGroup).channel(classOf[NioServerSocketChannel]).childHandler(new ChannelInitializer[SocketChannel]() {
      override def initChannel(ch: SocketChannel) {
        ch.pipeline.addLast(new ChannelInboundHandlerAdapter {
          override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
            val required = "list\n"
            val out = {
              val m = msg.asInstanceOf[ByteBuf]
              try {
                m.toString(java.nio.charset.Charset.defaultCharset())
              } finally {
                m.release()
              }
            }
            if ( out != required) {
              ctx.close()
            } else {
              val futures = for {text <- respondingWith} yield {
                val bytes = s"$text\n".getBytes("UTF-8")
                ctx.writeAndFlush(Unpooled.copiedBuffer(bytes)).sync()
              }
              for {lastFuture <- futures.lastOption} {
                lastFuture.addListener(new ChannelFutureListener {
                  override def operationComplete(future: ChannelFuture): Unit = {
                    ctx.close()
                  }
                })
              }
              if ( futures.length == 0 ) {
                ctx.close
              }
            }
          }
          override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cause.printStackTrace()
            ctx.close()
          }
        })
      }
    })

    (g, g.bind(9919).sync())
  }
}
