package us.woop.pinger

import akka.actor.ActorDSL._
import akka.actor.{ActorRef, Props}
import akka.testkit.TestKit

trait Thing {
  this: TestKit =>

  def parentedProbe(props: Props): ActorRef = {

    // create a layer for forwarding messages back
    actor(new Act {
      testActor ! context.actorOf(props)
      become { case any => testActor forward any }
    })

    expectMsgClass(classOf[ActorRef])

  }
}
