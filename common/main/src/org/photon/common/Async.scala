package org.photon.common

import java.util.concurrent.Executor
import com.twitter.util.{Try, Promise, Future}

object Async {
	import JavaConversion.fn2runnable

	def apply[T](fn: => T)(implicit e: Executor): Future[T] = {
		val promise = Promise[T]
		e.execute(promise update Try(fn))
		promise
	}
}
