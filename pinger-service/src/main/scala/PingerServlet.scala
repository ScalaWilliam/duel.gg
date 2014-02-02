import akka.actor.{ActorRef, Stash, Props, ActorSystem}
//import akka.contrib.pattern.DistributedPubSubExtension

import akka.event.LoggingReceive
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import javax.servlet.{ServletConfig, ServletResponse, ServletRequest, Servlet}
import akka.actor.ActorDSL._
/** 01/02/14 */
class PingerServlet extends HttpServlet {
  lazy val actorSystem: ActorSystem = {
    println("Initialising actor system...")
    ActorSystem("pingerMaster")
  }
  lazy val listener = actor(actorSystem, name="router"){new ActWithStash {
      //val mediator = DistributedPubSubExtension(context.system).mediator
      val subscribers = scala.collection.mutable.HashSet[ActorRef]()
      import akka.contrib.pattern.DistributedPubSubMediator._
      become (LoggingReceive {
        case PingerActor.Ready(on) =>
          val pinger = sender
          unstashAll()
          become (LoggingReceive {
            case pingRequest: PingerActor.Ping =>
              pinger ! pingRequest
            case subscribe: ActorRef =>
              subscribers += subscribe
            case message if sender == pinger =>
              for { subscriber <- subscribers }
                subscriber ! message
              //mediator ! Publish("serverResponse", message)
          })
        case msg =>

          stash()
      })
    }}
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.getOutputStream.println(s"OK: $listener // $actorSystem")
  }
  override def init(config: ServletConfig): Unit = {
    actorSystem.actorOf(Props(classOf[PingerActor], listener))
    super.init(config)
  }
  override def destroy() {
    actorSystem.shutdown()
  }
}
