package org.photon.login

import akka.actor.{Props, ActorLogging, Actor}
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress

class NetworkingActor extends Actor with ActorLogging {
  import Tcp._

  val manager = IO(Tcp)

  manager ! Bind(self, new InetSocketAddress(5555))

  def receive = {
    case Bound(addr) => log.info("successfully bound on {}", addr)

    case CommandFailed(Bind(_, addr, _, _)) => {
      log.error("can't bind on {}", addr)
      context stop self
    }

    case Connect(remoteAddr, _, _, _) => {
      log.debug("new incoming connection from {}", remoteAddr)

      val handler = context actorOf Props(classOf[NetworkHandlingActor], sender, remoteAddr)
      sender ! Register(handler)
    }
  }
}
