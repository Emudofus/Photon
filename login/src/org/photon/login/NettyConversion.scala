package org.photon.login

import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import com.twitter.util.Promise

object NettyConversion {

  implicit def fn2GenericFutureListener(fn: ChannelFuture => Unit) = new GenericFutureListener[ChannelFuture] {
    def operationComplete(future: ChannelFuture) = fn(future)
  }


  implicit class ChannelFutureExtension(val fut: ChannelFuture) extends AnyVal {
    def onCompleted(fn: ChannelFuture => Unit) = fut.addListener(fn)

    def toTw[T](fn: => T, promise: Promise[T] = Promise[T]): Promise[T] = {
      onCompleted(_ => promise.setValue(fn))
      promise
    }
  }

}
