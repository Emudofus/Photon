package org.photon.staticdata.converter

import java.util.concurrent.{Executors, Executor}
import org.photon.staticdata.converter.ancestra.MapDataLoader
import java.io.File
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.photon.jackson.flatjson.FlatJsonModule

object Converter {
	sealed trait Action

	case class Config(
		driver: String = null,
		url: String = null,
		action: Action = null
 	)

	val parser = new scopt.OptionParser[Config]("converter") {
		help("help")

		opt[String]('d', "driver")
			.required()
			.text("the JDBC driver used for database connection")
			.action { case (driver, config) => config.copy(driver = driver) }

		opt[String]("url")
			.required()
			.text("the URL used for database connection")
			.action { case (url, config) => config.copy(url = url) }

		cmd("maps")
			.text("this command will produce map data JSON")
			.children(
				opt[File]('o', "out")
					.required()
					.text("the file where map data JSON will be written")
					.action { case (out, config) =>
						config.copy(action = config.action.asInstanceOf[CreateMapsAction]
							.copy(out = out))
					}
			)
			.action { case (_, config) => config.copy(action = CreateMapsAction()) }
	}

	case class CreateMapsAction(out: File = null) extends Action


	def main(args: Array[String]) {
		implicit val e: Executor = Executors.newCachedThreadPool()

		val config = parser.parse(args.toSeq, Config()).getOrElse(sys.exit(1))

		Class.forName(config.driver)
		val connection = java.sql.DriverManager.getConnection(config.url)

		val mapper = new ObjectMapper
		mapper.registerModule(DefaultScalaModule)
		mapper.registerModule(new FlatJsonModule)

		config.action match {
			case CreateMapsAction(out) =>
				val loader = new MapDataLoader(connection)

				loader.load()
					.map { _.map(it => (it.id, it)).toMap }
					.onSuccess(mapper.writeValue(out, _))
					.onFailure(_.printStackTrace())
		}
	}
}