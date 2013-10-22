package org.photon.login

import com.typesafe.config.ConfigFactory
import org.photon.protocol.login._
import com.twitter.util.{Future, Await}
import org.photon.protocol.DofusProtocol

object LoginServer {
  def main(args: Array[String]) {
    import JavaConversion.RuntimeExtension

    val component = new Object
      with ConfigurationComponent
      with UserAuthenticationComponent
      with NetworkComponentImpl
      with HandlerComponentImpl
    {
      val config = ConfigFactory.load()

      def authenticate(s: NetworkSession, username: String, password: String): Future[Unit] = Future(???)
    }

    component.networkService.boot() onSuccess { s =>
      Runtime.getRuntime.onShutdown(s.kill)
    }
  }
}
