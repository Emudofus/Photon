package org.photon.realm

import org.photon.common.components.{Service, ServiceManagerComponent}
import com.twitter.util.{Await, Future}
import com.typesafe.config.ConfigFactory
import java.io.File

object RealmServer {
  def main(args: Array[String]) {
    val component = new Object
      with ConfigurationComponent
      with ServiceManagerComponent
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
