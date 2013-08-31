package org.photon.login

import akka.actor.{Props, ActorSystem}

object LoginServer {
  def main(args: Array[String]) {
    val system = ActorSystem("PhotonLoginServer")

    system.actorOf(Props[NetworkingActor], name = "network")
  }
}
