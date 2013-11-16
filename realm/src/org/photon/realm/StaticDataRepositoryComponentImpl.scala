package org.photon.realm

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import org.photon.common.components.{ExecutorComponent, ServiceManagerComponent, Service}
import org.photon.common.Async
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import java.io.File

trait StaticDataRepositoryComponentImpl extends StaticDataRepositoryComponent {
  self: ServiceManagerComponent with ExecutorComponent with ConfigurationComponent =>

  lazy val staticDataConfig = config.getConfig("photon.static-data")
  lazy val staticDataMapsPath = config.getString("maps")

  lazy val staticDataRepo = new StaticDataRepositoryImpl
  services += staticDataRepo

  class StaticDataRepositoryImpl extends StaticDataRepository with Service {

    type ObjectMapper = com.fasterxml.jackson.databind.ObjectMapper with ScalaObjectMapper

    trait CacheUp { self: ScalaObjectMapper =>
      private[this] val cache = mutable.Map.empty[Class[_], mutable.ArrayBuffer[Any]]

      def cacheUp[T](src: File)(implicit m: Manifest[T]) {
        cache.getOrElseUpdate(m.runtimeClass, mutable.ArrayBuffer.empty) ++= self.readValue[Seq[T]](src)
      }

      def theCache = cache.mapValues(_.toSeq)
    }

    def boot() = Async {
      /*val mapper = new com.fasterxml.jackson.databind.ObjectMapper with ScalaObjectMapper with CacheUp {}
      mapper.registerModule(new FlatJsonModule)
      mapper.registerModule(DefaultScalaModule)

      mapper.cacheUp[MapData](new File("lol"))
      val cache = mapper.theCache*/
    }

    def kill() = Async {

    }

    def first[T: ClassTag](fn: (T) => Boolean) = {
      ???
    }

    def where[T, Result](fn: (T) => Boolean)(implicit ct: ClassTag[T], cbf: CanBuildFrom[_, T, Result]) = {
      val builder = cbf()

      builder.result
    }
  }
}
