package org.photon.common

import scala.collection.mutable
import java.util.concurrent.locks.ReentrantReadWriteLock

object Observable {
  type Listener = PartialFunction[Any, Any]
  type UnitListener = PartialFunction[Any, Unit]
  type Lid = Long

  sealed case class UnavailableEventException(tpe: Symbol) extends RuntimeException
}

trait Observable {
  import Observable._
  import JavaConversion._

  private var nextLid = 0L
  private val l = new ReentrantReadWriteLock
  private val subs = mutable.Map.empty[Symbol, mutable.Map[Lid, Listener]]

  def subscribe(tpe: Symbol, fn: Listener): Lid = {
    l.write {
      val listeners = subs.getOrElse(tpe, throw UnavailableEventException(tpe))
      nextLid += 1
      listeners(nextLid) = fn
      nextLid
    }
  }

  def listen(tpe: Symbol)(fn: Listener): Lid = subscribe(tpe, fn)

  def unsubscribe(tpe: Symbol, lid: Lid) {
    l.write {
      for (listeners <- subs.get(tpe)) {
        listeners -= lid
      }
    }
  }

  def unsubscribe(tpe: Symbol, fn: Listener) {
    l.write {
      for (
        listeners <- subs.get(tpe);
        (lid, _) <- listeners.find { case (_, it) => it == fn }
      ) listeners -= lid
    }
  }

  def emitted(tpe: Symbol, types: Symbol*) {
    l.write {
      for (t <- types :+ tpe) {
        subs.getOrElseUpdate(t, mutable.Map.empty)
      }
    }
  }

  def emit(tpe: Symbol, args: Any = ()) {
    l.read {
      val listeners = subs.getOrElse(tpe, throw UnavailableEventException(tpe)).values
      for (
        listener <- listeners
        if listener.isDefinedAt(args)
      ) listener(args)
    }
  }
}