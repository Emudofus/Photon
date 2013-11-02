package org.photon.login

import org.apache.mina.core.future.{IoFutureListener, IoFuture}
import com.twitter.util.{Future, Promise}

object MinaConversion {
  implicit def fn2IoFutureListener[T <: IoFuture, R](fn: T => R) = new IoFutureListener[T] {
    def operationComplete(future: T) {
      fn(future)
    }
  }

  implicit class RichIoFuture[T <: IoFuture](val fut: T) extends AnyVal {
    def addListener[R](fn: T => R) {
      fut.addListener(fn)
    }

    def onCompleted[R](fn: => R) {
      fut.addListener { _: T => fn }
    }

    def toTw[R](fn: => R, p: Promise[R] = Promise[R]): Future[R] = {
      onCompleted(p setValue fn)
      p
    }
  }
}
