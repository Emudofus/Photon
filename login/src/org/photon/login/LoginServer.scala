package org.photon.login

import com.typesafe.config.ConfigFactory
import com.twitter.util.Future
import com.typesafe.scalalogging.slf4j.Logging
import org.slf4j.LoggerFactory

object LoginServer {
  def main(args: Array[String]) {
    val component = new Object
      with ConfigurationComponent
      with UserRepositoryComponentImpl
      with UserAuthenticationComponentImpl
      with NetworkComponentImpl
      with HandlerComponentImpl
    {
      lazy val config = ConfigFactory.load()
    }

    component.networkService.boot() onSuccess { s =>
      sys.addShutdownHook { s.kill() }
    }
  }
}
