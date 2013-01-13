package com.hyperscalelogic.soundstrip.view

import android.widget.ExpandableListView
import android.content.res.TypedArray
import android.util.AttributeSet
import android.content.Context
import android.view.DragEvent
import android.graphics.Canvas

class ExpListView(context: Context, attrs: AttributeSet)
  extends ExpandableListView(context, attrs) {

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
  }

  override def onDragEvent(event: DragEvent): Boolean = {
    super.onDragEvent(event)
  }
}
