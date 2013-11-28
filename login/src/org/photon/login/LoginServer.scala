package org.photon.login

import com.typesafe.config.ConfigFactory
import java.io.File
import org.photon.common.components._

trait LoginServerComponent extends AnyRef
  with ConfigurationComponent
  with ServiceManagerComponent
  with ExecutorComponentImpl
  with DatabaseComponentImpl
  with UserRepositoryComponentImpl
  with UserAuthenticationComponentImpl
  with NetworkComponentImpl
  with RealmManagerComponentImpl
  with HandlerComponentImpl

object LoginServer extends LoginServerComponent with BootableComponent {
  lazy val config = sys.props.get("photon.config")
    .map { file => ConfigFactory.parseFile(new File(file)) }
    .getOrElse(ConfigFactory.empty())
    .withFallback(ConfigFactory.load())

  lazy val databaseUrl = config.getString("photon.database.url")
  lazy val databaseDriver = config.getString("photon.database.driver")

  def main(args: Array[String]) = boot()
}
