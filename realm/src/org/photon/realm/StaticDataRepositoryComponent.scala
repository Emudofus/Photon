package org.photon.realm

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom

trait StaticDataRepository {
  def first[T: ClassTag](fn: T => Boolean): T
  def where[T, Result](fn: T => Boolean)(implicit ct: ClassTag[T], cbf: CanBuildFrom[_, T, Result]): Result
}

trait StaticDataRepositoryComponent {
  val staticDataRepo: StaticDataRepository
}
