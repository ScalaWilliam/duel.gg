import java.nio.{ByteOrder, ByteBuffer}
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler, ServletHolder}
import org.eclipse.jetty.webapp.WebAppContext

object TestRun extends App {

  val servah = new Server(55555)
  val handlerList = new HandlerList
  servah.setHandler(handlerList)

  case class Key(index: Long, seq: Int, ip: String, port: Int)
  def decodeKey(key: Array[Byte])  = {
    val bb = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN)
    val idx = bb.getLong
    val seq = bb.getInt
    val ip = ByteBuffer.allocate(4)
    bb.get(ip.array(), 0, 4)
    val port = bb.getInt
    Key(idx, seq, ip.array().map{_.toInt & 0xFF}.mkString("."), port)
  }

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)

  context.addServlet(
    new ServletHolder(new vaadin.scala.server.ScaladinServlet()) {
      this.setInitParameter("ScaladinUI", "TestBrowser")
    }, "/*"
  )
  val staticHandler = new ServletContextHandler {
    setContextPath("/VAADIN")
    setBaseResource(org.eclipse.jetty.util.resource.Resource.newClassPathResource("/"))
    addServlet(classOf[DefaultServlet], "/")
  }


  handlerList.addHandler(context)
  handlerList.addHandler(staticHandler)

  servah.start()
  servah.join()
}
