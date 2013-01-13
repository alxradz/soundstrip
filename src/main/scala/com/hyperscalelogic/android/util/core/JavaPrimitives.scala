package com.hyperscalelogic.android.util.core

object JavaPrimitives {

  implicit def javaToScalaInt(d: java.lang.Integer) = d.intValue

  implicit def javaToScalaLong(d: java.lang.Long) = d.longValue

}
