package org.photon.login

import com.typesafe.config.ConfigFactory
import com.twitter.util.Future
import com.typesafe.scalalogging.slf4j.Logging
import org.slf4j.LoggerFactory

object LoginServer {
  def main(args: Array[String]) {
    val component = new Object
      with ConfigurationComponent
      with UserAuthenticationComponent
      with NetworkComponentImpl
      with HandlerComponentImpl
    {
      lazy val config = ConfigFactory.load()

      def authenticate(s: NetworkSession, username: String, password: String): Future[Unit] = Future(???)
    }

    component.networkService.boot() onSuccess { s =>
      sys.addShutdownHook { s.kill() }
    }
  }
}
