package org.photon.realm

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import org.photon.common.components.Service

trait DataRepository extends Service {
	def apply[T: ClassTag](id: Any): Option[T]
	def where[T, Result](fn: T => Boolean)(implicit ev: ClassTag[T], cbf: CanBuildFrom[_, T, Result]): Result
}

trait DataRepositoryComponent {
	implicit val dataRepository: DataRepository
}