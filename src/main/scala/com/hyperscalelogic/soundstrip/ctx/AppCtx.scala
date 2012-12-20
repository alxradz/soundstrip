package com.hyperscalelogic.soundstrip.ctx

import com.google.common.cache.LoadingCache
import android.graphics.{BitmapFactory, Bitmap}
import android.graphics.Bitmap.{createScaledBitmap => scaleBitmap}

import android.content.{Context => DroidCtx}
import java.util.concurrent.Executors
import android.os.Handler
import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.lang.IllegalStateException
import com.hyperscalelogic.android.util.log.Log
import com.hyperscalelogic.android.util.guava.RichCacheBuilder._
import com.hyperscalelogic.android.util.core.ConcurrentUtil.runnable
import com.hyperscalelogic.soundstrip.cfg.AppCfg

trait AppCtx {
  val textEnricher: TextEnricher
  def loadBitmapThenPost(file: String, w: Int = -1, h: Int = -1, f: (Bitmap) => Unit)
  def enqeueTask(msg: String, f: () => Unit)
}

object AppCtx {
  val log = Log(this.getClass.getSimpleName)

  var handler: Handler = null

  def init(h: Handler) {
    if (handler != null && h != handler) throw new IllegalStateException("App context already initialised!")
    handler = h
  }

  lazy val instance: AppCtx = new DroidAppCtx
  def apply(): AppCtx = instance

}

private class DroidAppCtx extends AppCtx {

  import AppCtx.log

  private val bgexec = Executors.newFixedThreadPool(1,
    new ThreadFactoryBuilder()
      .setDaemon(true)
      .setNameFormat("bgexec-%d")
      .setPriority(Thread.NORM_PRIORITY)
      .build())

  private val uiexec = new Handler()
  log("ctor").debug("UI executor thread=%s", uiexec.getLooper.getThread.getName)

  private val bitmapCache: LoadingCache[String, Bitmap] =
    newCacheBuilder()
      .recordStats()
      .weigher((k: String, v: Bitmap) => v.getByteCount)
      .maximumWeight(AppCfg.APP_CTX_BITMAP_CACHE_SIZE_BYTES)
      .build((k: String) => BitmapFactory.decodeFile(k))

  val textEnricher = TextEnricher()

  def loadBitmapThenPost(file: String, w: Int = -1, h: Int = -1, f: (Bitmap) => Unit) {
    enqeueTask("Loading bitmap: " + file, () => {
      val bm = if (w > 0 && h > 0) scaleBitmap(bitmapCache.get(file), w, h, true) else bitmapCache.get(file)
      uiexec.post(runnable("Applying bitmap: " + bm, () => f(bm)))
    })
  }

  def enqeueTask(msg: String, f: () => Unit) {
    try {
      bgexec.execute(runnable(msg, f))
    } catch {
      case e: Exception => log("enqueue").error(e, "Failed to enqueue task: " + msg)
    }
  }

}
