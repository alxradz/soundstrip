package com.hyperscalelogic.android.util.log

import android.util.{Log => ALog}

class Printer(app: String, ctx: String, func: String) {

  val tag = "%s:%s:%s".format(app, ctx, func)

  def info(msg: String, args: Any*) {
    ALog.i(tag, msg.format(args: _*))
  }

  def debug(msg: String, args: Any*) {
    ALog.d(tag, msg.format(args: _*))
  }

  def trace(msg: String, args: Any*) {
    ALog.v(tag, msg.format(args: _*))
  }

  def warn(msg: String, args: Any*) {
    ALog.w(tag, msg.format(args: _*))
  }

  def warn(t: Throwable, msg: String, args: Any*) {
    ALog.w(tag, msg.format(args: _*))
  }

  def error(msg: String, args: Any*) {
    ALog.e(tag, msg.format(args: _*))
  }

  def error(t: Throwable, msg: String, args: Any*) {
    ALog.e(tag, msg.format(args: _*))
  }

  def fatal(msg: String, args: Any*) {
    ALog.wtf(tag, msg.format(args: _*))
  }

  def fatal(t: Throwable, msg: String, args: Any*) {
    ALog.wtf(tag, msg.format(args: _*))
  }

}

object Log {
  val map: Map[String, Log] = Map()

  var app = "APP"

  def init(a: String) {
    app = a
  }

  def apply(ctx: String): Log = {
    map.getOrElse(ctx, new Log(app, ctx.replace("$", "")))
  }
}

class Log(app: String, ctx: String) {
  val map: Map[String, Printer] = Map()

  def apply(func: String): Printer = map.getOrElse(func, new Printer(app, ctx, func))

}

