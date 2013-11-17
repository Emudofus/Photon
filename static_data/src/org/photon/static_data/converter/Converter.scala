package org.photon.static_data.converter

import com.twitter.util.Future
import scala.collection.generic.CanBuildFrom

trait Converter[T] {
  def convert[Result](implicit cbf: CanBuildFrom[_, T, Result]): Future[Result]
}
