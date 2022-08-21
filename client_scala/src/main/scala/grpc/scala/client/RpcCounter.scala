package grpc.scala.client

import java.util.concurrent.atomic.AtomicLong

class RpcCounter {
  val counter = new AtomicLong(0)
  def get() = counter.get()
  def set(v: Long) = counter.set(v)
  def inc() = counter.incrementAndGet()
  def modify(f: Long => Long) = {
    var done = false
    var oldVal: Long = 0
    while (!done) {
      oldVal = counter.get()
      done = counter.compareAndSet(oldVal, f(oldVal))
    }
  }
}
