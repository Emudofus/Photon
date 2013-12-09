package org.photon.realm

import org.photon.common.components._
import com.typesafe.config.ConfigFactory
import java.io.File

trait RealmServerComponent extends AnyRef
	with ConfigurationComponent
	with ServiceManagerComponent
	with DatabaseComponentImpl
	with ExecutorComponentImpl
	with DataRepositoryComponentImpl
	with PlayerRepositoryComponentImpl
	with LoginManagerComponentImpl
	with HandlerComponentImpl
	with NetworkComponentImpl

object RealmServer extends RealmServerComponent with BootableComponent {
	lazy val config = sys.props.get("photon.config")
		.map { file => ConfigFactory.parseFile(new File(file)) }
		.getOrElse(ConfigFactory.empty())
		.withFallback(ConfigFactory.load())

	lazy val databaseUrl = config.getString("photon.database.url")
	lazy val databaseDriver = config.getString("photon.database.driver")

	def main(args: Array[String]) = boot()
}
