package com.hyperscalelogic.soundstrip.adapter

import android.database.DataSetObserver
import android.view.{ViewGroup, View}
import com.hyperscalelogic.soundstrip.data.Album

class AlbumExpListAdapter extends ModExpListAdapter[Album, Unit] {

  def add(elem: Album, notify: Boolean) {}
  def add(elems: Seq[Album]) {}

  def areAllItemsEnabled(): Boolean = false

  def getChild(groupPosition: Int, childPosition: Int): AnyRef = null
  def getChildId(groupPosition: Int, childPosition: Int): Long = 0L
  def getChildrenCount(groupPosition: Int): Int = 0
  def getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View, parent: ViewGroup): View = null

  def getCombinedChildId(groupId: Long, childId: Long): Long = 0L
  def getCombinedGroupId(groupId: Long): Long = 0L

  def getGroup(groupPosition: Int): AnyRef = null
  def getGroupCount: Int = 0
  def getGroupId(groupPosition: Int): Long = 0L
  def getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup): View = null

  def hasStableIds: Boolean = false
  def isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
  def isEmpty: Boolean = false
  def onGroupCollapsed(groupPosition: Int) {}
  def onGroupExpanded(groupPosition: Int) {}
  def registerDataSetObserver(observer: DataSetObserver) {}
  def unregisterDataSetObserver(observer: DataSetObserver) {}
}
