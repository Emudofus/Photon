package org.photon.login

import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import com.twitter.util.Promise

object NettyConversion {


  implicit class ChannelFutureExtension(val fut: ChannelFuture) extends AnyVal {
    def onCompleted(fn: ChannelFuture => Unit) = fut.addListener(new GenericFutureListener[Nothing] {
      def operationComplete(future: Nothing) = fn(fut)
    })

    def toTw[T](fn: => T, promise: Promise[T] = Promise[T]): Promise[T] = {
      onCompleted(_ => promise.setValue(fn))
      promise
    }
  }

}
