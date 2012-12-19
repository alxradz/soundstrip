package com.hyperscalelogic.soundstrip.view

import android.database.DataSetObserver
import android.view.{ViewGroup, View}

class SynchronizedListAdapter[T](other: ModifiableListAdapter[T]) extends ModifiableListAdapter[T] {

  def getCount: Int = this.synchronized(other.getCount)

  def getItem(position: Int): AnyRef = this.synchronized(other.getItem(position))

  def getItemId(position: Int): Long = this.synchronized(other.getItemId(position))

  def getItemViewType(position: Int): Int = this.synchronized(other.getItemViewType(position))

  def getView(position: Int, convertView: View, parent: ViewGroup): View = this.synchronized(other.getView(position, convertView, parent))

  def getViewTypeCount: Int = this.synchronized(other.getViewTypeCount)

  def hasStableIds: Boolean = this.synchronized(other.hasStableIds)

  def isEmpty: Boolean = this.synchronized(other.isEmpty)

  def areAllItemsEnabled(): Boolean = this.synchronized(other.areAllItemsEnabled())

  def isEnabled(position: Int): Boolean = this.synchronized(other.isEnabled(position))

  def registerDataSetObserver(observer: DataSetObserver) {
    this.synchronized(other.registerDataSetObserver(observer))
  }

  def unregisterDataSetObserver(observer: DataSetObserver) {
    this.synchronized(other.unregisterDataSetObserver(observer))
  }

  def add(elem: T, notify: Boolean) {
    this.synchronized(other.add(elem, notify))
  }

  def add(elems: Seq[T]) {
    this.synchronized(other.add(elems))
  }
}
