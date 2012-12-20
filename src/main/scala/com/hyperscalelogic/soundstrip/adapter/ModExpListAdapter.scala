package com.hyperscalelogic.soundstrip.adapter

import android.widget.ExpandableListAdapter

trait ModExpListAdapter[R, C] extends ExpandableListAdapter {
  def add(elems: Seq[R])
  def add(elem: R, notify: Boolean = true)
}

