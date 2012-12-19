package com.hyperscalelogic.android.util.guava

import com.google.common.cache.{CacheBuilder, CacheLoader, Weigher}
import java.util.concurrent.Callable

object RichCacheBuilder {

  def newCacheBuilder() = CacheBuilder.newBuilder()

  def weigher[K, V](f: (K, V) => Int): Weigher[K, V] = new Weigher[K, V] {
    def weigh(key: K, value: V): Int = f(key, value)
  }

  def loader[K, V](f: (K) => V): CacheLoader[K, V] = new CacheLoader[K, V] {
    def load(key: K): V = f(key)
  }

}