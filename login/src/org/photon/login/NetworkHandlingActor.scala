package org.photon.login

import akka.actor.{FSM, Actor, ActorRef}
import java.net.InetSocketAddress
import org.photon.protocol.login.HelloConnectMessage

class NetworkHandlingActor(remote: ActorRef, remoteAddr: InetSocketAddress) extends Actor with FSM[] {
  val ticket: String = "abcd"

  remote ! HelloConnectMessage(ticket)

  import akka.io.Tcp._
  context become versionCheck

  def versionCheck: Receive = {
    case Received(data) =>
  }

  def receive = ???
}
