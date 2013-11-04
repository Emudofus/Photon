package org.photon.login

import com.typesafe.config.ConfigFactory
import com.twitter.util.JavaTimer

object LoginServer {
  def main(args: Array[String]) {
    import com.twitter.util.TimeConversions._

    implicit val timer = new JavaTimer(false)

    val component = new Object
      with ConfigurationComponent
      with ExecutorComponentImpl
      with DatabaseComponentImpl
      with UserRepositoryComponentImpl
      with UserAuthenticationComponentImpl
      with NetworkComponentImpl
      with RealmManagerComponentImpl
      with HandlerComponentImpl
    {
      lazy val config = ConfigFactory.load()
    }

    component.networkService.boot().within(1 second) onSuccess { s =>
      sys.addShutdownHook { s.kill() }
    }
  }
}
