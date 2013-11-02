package org.photon.login

import com.twitter.util.{Try, Promise, Future}
import java.util.concurrent.Executor

object Async {
  import JavaConversion.fn2runnable

  def apply[T](fn: => T)(implicit e: Executor): Future[T] = {
    val promise = Promise[T]
    e.execute(promise update Try(fn))
    promise
  }
}
