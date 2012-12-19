package com.hyperscalelogic.android.util.core

import com.hyperscalelogic.android.util.log.{Log, Printer}
import java.util.concurrent.Callable

object ConcurrentUtil {
  private val log = Log(this.getClass.getSimpleName)

  def runnable(msg: String, f: () => Unit) = new Runnable() {
    val llog: Printer = log("runnable")
    def run() {
      try {
        llog.debug("Running: %s ...", msg)
        f()
        llog.debug("Completed: %s.", msg)
      } catch {
        case t: Throwable => llog.error(t, "Error while running: %s", msg)
      }
    }
  }

  def callable[T](msg: String, f: () => T) = new Callable[T]() {
    val llog: Printer = log("callable")
    def call(): T = {
      try {
        llog.debug("Running: %s ...", msg)
        val rval = f()
        llog.debug("Completed: %s.", msg)
        rval
      } catch {
        case t: Throwable => llog.error(t, "Error while calling: %s", msg); throw t
      }
    }
  }

}
