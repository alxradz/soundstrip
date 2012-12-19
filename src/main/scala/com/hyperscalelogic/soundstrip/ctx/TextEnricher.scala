package com.hyperscalelogic.soundstrip.ctx

import com.google.common.cache.Cache
import com.hyperscalelogic.android.util.guava.RichCacheBuilder._
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.hyperscalelogic.android.util.graphics.ColorUtil._
import java.util.concurrent.Callable

trait TextEnricher {
  def shadeBrackets(str: String, color: Int, scale: Float): SpannableString
}

object TextEnricher {
  val instance = new TextEnricherImpl

  def apply(): TextEnricher = instance
}

class TextEnricherImpl extends TextEnricher {

  val cache: Cache[String, SpannableString] =
    newCacheBuilder()
      .recordStats()
      .weigher(weigher[String, SpannableString]((k, v) => v.length()))
      .maximumWeight(128 * 1024 * 1024)
      .build()

  def shadeBrackets(str: String, color: Int, scale: Float): SpannableString = {
    val key = "BRACK" + str

    cache.get(key, new Callable[SpannableString] {
      def call(): SpannableString = {
        var oparen: Int = -1
        var cparen: Int = -1
        var obrack: Int = -1
        var cbrack: Int = -1

        var i = 0
        str.foreach(c => {
          c match {
            case '(' => oparen = i
            case ')' => cparen = i + 1
            case '[' => obrack = i
            case ']' => cbrack = i + 1
            case _ => {}
          }
          i += 1
        })

        val richText = new SpannableString(str)
        if (oparen >= 0 && cparen > oparen) {
          richText.setSpan(
            new ForegroundColorSpan(shadeColor(color, scale)),
            oparen,
            cparen,
            0)
        }

        if (obrack >= 0 && cbrack > obrack) {
          richText.setSpan(
            new ForegroundColorSpan(shadeColor(color, scale)),
            obrack,
            cbrack,
            0)
        }

        richText
      }
    })

  }
}
