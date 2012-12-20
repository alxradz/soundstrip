package com.hyperscalelogic.soundstrip.adapter

import android.widget.ListAdapter

trait ModListAdapter[T] extends ListAdapter {
  def add(elems: Seq[T])
  def add(elem: T, notify: Boolean = true)
}
