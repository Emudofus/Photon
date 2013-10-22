package org.photon.login

object JavaConversion {
  implicit def fn2runnable(fn: => Unit) = new Runnable {
    def run = fn
  }

  implicit class RuntimeExtension(val runtime: Runtime) extends AnyVal {
    def onShutdown(fn: => Unit) = runtime.addShutdownHook(new Thread(fn: Runnable))
  }
}
