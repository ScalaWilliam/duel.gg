import akka.actor.ActorSystem
import akka.contrib.pattern.DistributedPubSubExtension
import akka.event.LoggingReceive
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.{ServletConfig, ServletResponse, ServletRequest, Servlet}
import akka.actor.ActorDSL._
class PingerClientServlet extends HttpServlet {

  lazy val actorSystem = ActorSystem("pingerMaster")

  val data = scala.collection.mutable.MutableList[Any]()
  lazy val client = actor(actorSystem, name = "bunga-client"){new ActWithStash {
    //val mediator = DistributedPubSubExtension(context.system).mediator
    //import akka.contrib.pattern.DistributedPubSubMediator._
//    //mediator ! Subscribe("serverResponse", self)
//    context.actorSelection("router") ! PingerActor.Ping("81.169.137.114", 30000)
//    context.actorSelection("router") ! PingerActor.Ping("85.214.66.181",10000)
    become (LoggingReceive  {
      case msg => data += msg
    })
  }}

  override def init(config: ServletConfig) {
    super.init(config)


  }
  override def destroy() {
    actorSystem.shutdown()
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    val os = resp.getOutputStream
    if ( Option(req.getQueryString).getOrElse("").contains("ping") ) {
      val wouter = actorSystem.actorSelection("/user/router")
      os.println(s"$wouter")
      wouter ! client
      wouter ! PingerActor.Ping("81.169.137.114", 30000)
      wouter ! PingerActor.Ping("85.214.66.181",10000)
    }
    os.println(s"$actorSystem")
    os.println(s"$data")
    os.println(s"$client")
  }


}