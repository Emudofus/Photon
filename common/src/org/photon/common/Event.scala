package org.photon.common

import scala.collection.mutable
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.Executor
import com.twitter.util.Future
import java.util.concurrent.locks.ReentrantReadWriteLock

trait Event {
  def apply(event: Any): Future[Seq[Any]]
  def +=(fn: Event.Listener): EventSubscription
}

trait EventSubscription {
  def event: Event
  def unsubscribe()
}

object Event {
  type Listener = Any => Any
  type UnitListener = Any => Unit
  def newEvent(implicit e: Executor): Event = new EventImpl
}

private[common] class EventImpl(implicit e: Executor) extends Event {
  self =>
  import JavaConversion.RichReentrantReadWriteLock

  val nextId = new AtomicLong
  val subs = mutable.Map.empty[Long, self.Subscription]
  val l = new ReentrantReadWriteLock

  def apply(event: Any) = Async {
    l.read {
      subs.values.toStream.map(_.fn(event)).toSeq
    }
  }

  def +=(fn: Event.Listener) = {
    l.write {
      val sub = new self.Subscription(nextId.incrementAndGet, fn)
      subs(sub.id) = sub
      sub
    }
  }
  
  private[common] class Subscription(val id: Long, val fn: Event.Listener) extends EventSubscription {
    def event = self

    def unsubscribe() {
      l.write {
        subs -= id
      }
    }
  }
}

class EventSubscriptionBag private[common]() extends mutable.HashMap[Event, EventSubscription] {
  def update(event: Event, fn: Event.Listener) {
    update(event, event += fn)
  }
  
  def unsubscribe(event: Event) = remove(event) foreach (_.unsubscribe())
}

object EventSubscriptionBag {
  def empty = new EventSubscriptionBag
}