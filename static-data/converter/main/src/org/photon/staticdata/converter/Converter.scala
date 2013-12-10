package org.photon.staticdata.converter

import java.util.concurrent.{Executors, Executor}
import org.photon.staticdata.converter.ancestra.MapDataLoader
import java.io.File
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.photon.jackson.flatjson.FlatJsonModule
import com.twitter.util.NonFatal

object Converter {
	implicit val e: Executor = Executors.newCachedThreadPool()

	val baseFolder = new File(sys.props.getOrElse("conv.base", "."))
	val mapsFile = new File(baseFolder, sys.props.getOrElse("conv.maps", "maps.json"))

	Class.forName(sys.props("conv.driver"))
	val connection = java.sql.DriverManager.getConnection(sys.props("conv.url"))

	val mapper = new ObjectMapper
	mapper.registerModule(DefaultScalaModule)
	mapper.registerModule(new FlatJsonModule)

	def main(args: Array[String]) {
		implicit val mapLoader = new MapDataLoader(connection)

		mapLoader.load() onSuccess { maps =>
			mapper.writeValue(mapsFile, maps.map(it => (it.id, it)).toMap)
		} onFailure {
			case NonFatal(ex) =>
				ex.printStackTrace()
		}
	}
}