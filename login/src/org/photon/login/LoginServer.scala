package org.photon.login

import com.typesafe.config.ConfigFactory

object LoginServer {
  def main(args: Array[String]) {
    val component = new Object
      with ConfigurationComponent
      with ExecutorComponentImpl
      with DatabaseComponentImpl
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
