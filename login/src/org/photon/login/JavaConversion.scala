package org.photon.login

object JavaConversion {
  implicit def fn2runnable(fn: => Unit) = new Runnable {
    def run = fn
  }
}
