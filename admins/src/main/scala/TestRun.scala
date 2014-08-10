import java.nio.{ByteOrder, ByteBuffer}
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler, ServletHolder}
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.bridge.SLF4JBridgeHandler

object TestRun extends App {

  SLF4JBridgeHandler.install()

  val servah = new Server(55555)
  val handlerList = new HandlerList
  servah.setHandler(handlerList)

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)

  context.addServlet(
    new ServletHolder(new vaadin.scala.server.ScaladinServlet()) {
//      this.setInitParameter("UI", classOf[TestBrowser].getCanonicalName)
//      this.setInitParameter("UI", "TestBrowser")
      this.setInitParameter("pushmode", "automatic")
      this.setAsyncSupported(true)
      this.setInitParameter("ScaladinUIProvider", "TestProvider")
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
