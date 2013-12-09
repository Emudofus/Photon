package org.photon.realm

import org.photon.common.components.{ExecutorComponent, ServiceManagerComponent}
import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import com.twitter.util.Future
import org.photon.common.Async
import scala.collection.mutable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.photon.staticdata.MapData
import java.io.File

trait DataRepositoryComponentImpl extends DataRepositoryComponent {
	self: ServiceManagerComponent with ConfigurationComponent with ExecutorComponent =>

	private[this] val thisConfig = config.getConfig("photon.static-data")
	private val basePath = thisConfig.getString("base-path")
	private val mapsPath = basePath + "/" + thisConfig.getString("maps")

	implicit val dataRepository = new DataRepositoryImpl
	services += dataRepository

	class DataRepositoryImpl extends DataRepository {
		private[this] val map = mutable.Map.empty[Class[_], mutable.Map[Any, Any]]

		def boot() = Async {
			implicit val mapper = new ObjectMapper with ScalaObjectMapper
			mapper registerModule DefaultScalaModule

			register[MapData](mapsPath) { _.id }
		}

		def kill() = Future.Done

		def register[T](path: String)(keyOf: T => Any)(implicit ev: Manifest[T], mapper: ScalaObjectMapper) {
			val values = map.getOrElseUpdate(ev.runtimeClass, mutable.Map.empty)
			values ++= mapper.readValue[Seq[T]](new File(path)) map { it => (keyOf(it), it) }
		}

		def apply[T: ClassTag](id: Any): Option[T] = map.get(implicitly[ClassTag[T]].runtimeClass)
			.flatMap(_.get(id))
			.map(_.asInstanceOf[T])

		def where[T, Result](fn: (T) => Boolean)(implicit ev: ClassTag[T], cbf: CanBuildFrom[_, T, Result]): Result = {
			val builder = cbf()

			for (
				values <- map.get(ev.runtimeClass);
				(_, v) <- values
			) {
				val value = v.asInstanceOf[T]
				if (fn(value)) {
					builder += value
				}
			}

			builder.result()
		}
	}
}