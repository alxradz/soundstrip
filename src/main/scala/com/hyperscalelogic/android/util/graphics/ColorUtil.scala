package com.hyperscalelogic.android.util.graphics

import android.graphics.Color

object ColorUtil {

  def shadeColor(col: Int, scale: Float): Int = {
    val hsv = Array(0.0f, 0.0f, 0.0f)
    Color.colorToHSV(col, hsv)
    hsv(2) *= scale
    Color.HSVToColor(hsv)
  }

}
