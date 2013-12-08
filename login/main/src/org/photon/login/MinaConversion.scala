package org.photon.login

import org.apache.mina.core.future.{IoFutureListener, IoFuture}
import com.twitter.util.{Future, Promise}
import org.apache.mina.core.session.IoSession

object MinaConversion {
	import scala.language.implicitConversions

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

	trait Attr[T] {
		def key: String
	}

	object Attr {
		import scala.reflect.runtime.universe._

		def apply[T: TypeTag] = new Attr[T] {
			val key = implicitly[TypeTag[T]].tpe.typeSymbol.fullName
		}
	}

	implicit class RichIoSession[T <: IoSession](val s: T) extends AnyVal {
		def !(o: Any): Future[IoSession] = s.write(o).toTw(s)
		def !!(o: Any): Future[IoSession] = (this ! o) flatMap (x => x.close(true).toTw(x))

		def attr[A: Attr]: Option[A] = Option(s.getAttribute(implicitly[Attr[A]].key).asInstanceOf[A])
		def attr[A: Attr](o: Option[A]): Unit = s.setAttribute(implicitly[Attr[A]].key, o.orNull)
	}
}
