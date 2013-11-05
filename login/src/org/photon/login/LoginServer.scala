package org.photon.login

import com.typesafe.config.ConfigFactory
import com.twitter.util.{Future, Await}
import java.io.File

object LoginServer {
  def main(args: Array[String]) {
    val component = new Object
      with ConfigurationComponent
      with ServiceManagerComponent
      with ExecutorComponentImpl
      with DatabaseComponentImpl
      with UserRepositoryComponentImpl
      with UserAuthenticationComponentImpl
      with NetworkComponentImpl
      with RealmManagerComponentImpl
      with HandlerComponentImpl
    {
      lazy val config = sys.props.get("photon.config")
        .map { file => ConfigFactory.parseFile(new File(file)) }
        .getOrElse(ConfigFactory.empty())
        .withFallback(ConfigFactory.load())

      lazy val services = Seq.newBuilder[Service]
    }

    val services = component.services.result()

    Future.collect(services map {_.boot()}) onSuccess { _ =>
      sys.addShutdownHook {
        Await result Future.collect(services map {_.kill()})
      }
    }
  }
}
