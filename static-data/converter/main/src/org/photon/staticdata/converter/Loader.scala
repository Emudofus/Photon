package org.photon.staticdata.converter

import com.twitter.util.Future

trait Loader[T] {
	def load(): Future[Seq[T]]
}