package com.hyperscalelogic.soundstrip.view

import android.widget.ListAdapter

trait ModifiableListAdapter[T] extends ListAdapter {
  def add(elems: Seq[T])
  def add(elem: T, notify: Boolean = true)
}
