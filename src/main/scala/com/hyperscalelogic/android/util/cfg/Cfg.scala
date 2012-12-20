package com.hyperscalelogic.android.util.cfg

object Cfg {

  def intVal(p: String, defaultVal: Int): Int = {
    val str = System.getProperty(p)
    if (str == null) defaultVal else str.toInt
  }

  def floatVal(p: String, defaultVal: Float): Float = {
    val str = System.getProperty(p)
    if (str == null) defaultVal else str.toFloat
  }

  def strVal(p: String, defaultVal: String): String = {
    assert(defaultVal != null)

    val str = System.getProperty(p)
    if (str == null) defaultVal else str
  }

}