package org.photon.common

object JavaConversion {
  implicit def fn2runnable(fn: => Unit) = new Runnable {
    def run = fn
  }
}
