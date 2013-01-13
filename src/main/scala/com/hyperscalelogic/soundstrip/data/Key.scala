package com.hyperscalelogic.soundstrip.data

import com.google.common.primitives.UnsignedBytes

object Key {
  def apply[T <: DataObj](key: Array[Byte]) = new Key[T](key)
  def apply[T <: DataObj](key: String) = new Key[T](key.getBytes)
}

class Key[T <: DataObj](private val key: Array[Byte]) extends Ordered[Key[T]] {
  def compare(other: Key[T]): Int = UnsignedBytes.lexicographicalComparator().compare(key, other.key)
}
