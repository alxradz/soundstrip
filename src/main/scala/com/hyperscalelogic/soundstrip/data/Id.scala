package com.hyperscalelogic.soundstrip.data

object Id {
  def apply(id: Long) = new LongId(id)
}

trait Id {
  def asLong(): Long
}

class LongId(val id: Long) extends Id {

  override def equals(o: Any): Boolean = if (o.isInstanceOf[LongId]) id.equals(o.asInstanceOf[LongId].id) else false
  override def hashCode(): Int = 67 * id.hashCode
  override def toString: String = id.toString

  def asLong(): Long = id
}