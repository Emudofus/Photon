package org.photon.realm

import org.photon.common.components.{DatabaseComponentImpl, ExecutorComponentImpl, Service, ServiceManagerComponent}
import com.twitter.util.{Await, Future}
import com.typesafe.config.ConfigFactory
import java.io.File

object RealmServer {
  def main(args: Array[String]) {
    val component = new Object
      with ConfigurationComponent
      with ServiceManagerComponent
      with DatabaseComponentImpl
      with ExecutorComponentImpl
      with PlayerRepositoryComponentImpl
      with LoginManagerComponentImpl
      with HandlerComponentImpl
      with NetworkComponentImpl
    {
      lazy val config = sys.props.get("photon.config")
        .map { file => ConfigFactory.parseFile(new File(file)) }
        .getOrElse(ConfigFactory.empty())
        .withFallback(ConfigFactory.load())

      lazy val databaseUrl = config.getString("photon.database.url")
      lazy val databaseDriver = config.getString("photon.database.driver")

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
