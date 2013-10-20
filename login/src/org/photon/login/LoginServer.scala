package org.photon.login

import com.typesafe.config.ConfigFactory
import org.photon.protocol.login.{QueueStatusRequest, AuthenticationMessage}

object LoginServer {
  def main(args: Array[String]) {
    import HandlerComponent._

    val authHandler: NetworkHandler = {
      case Message(s, AuthenticationMessage(username, password)) =>

    }

    val queueHandler: NetworkHandler = {
      case Message(s, QueueStatusRequest) =>

    }

    val component = new ConfigurationComponent with HandlerComponent with NetworkComponentImpl {
      val config = ConfigFactory.load()
      val networkHandler: NetworkHandler = authHandler orElse queueHandler
    }

    component.networkService.boot() onSuccess { _ =>
      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run() {
          component.networkService.kill()
        }
      })
    } onFailure {
      case ex: NullPointerException =>
    }
  }
}
