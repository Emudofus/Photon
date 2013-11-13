package org.photon.realm

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import org.photon.common.components.{ExecutorComponent, ServiceManagerComponent, Service}
import org.photon.common.Async
import com.fasterxml.jackson.databind.ObjectMapper
import org.photon.jackson.flatjson.FlatJsonModule

trait StaticDataRepositoryComponentImpl extends StaticDataRepositoryComponent {
  self: ServiceManagerComponent with ExecutorComponent with ConfigurationComponent =>

  lazy val staticDataConfig = config.getConfig("photon.static-data")
  lazy val staticDataMapsPath = config.getString("maps")

  lazy val staticDataRepo = new StaticDataRepositoryImpl
  services += staticDataRepo

  class StaticDataRepositoryImpl extends StaticDataRepository with Service {
    private[this] val cache = mutable.Map.empty[Class[_], mutable.ArrayBuffer[Any]]

    def boot() = Async {
      val mapper = new ObjectMapper
      mapper.registerModule(new FlatJsonModule)
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
