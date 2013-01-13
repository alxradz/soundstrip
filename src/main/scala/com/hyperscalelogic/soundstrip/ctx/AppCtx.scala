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
import android.graphics.drawable.BitmapDrawable
import android.content.res.Resources
import com.hyperscalelogic.soundstrip.data.LongId

trait AppCtx {
  val textEnricher: TextEnricher
  def loadBitmapThenPost(file: String, w: Int = -1, h: Int = -1, f: (Bitmap) => Unit)
  def loadBitmapDrawableThenPost(path: String, w: Int = -1, h: Int = -1, f: (BitmapDrawable) => Unit)
  def enqeueTask(msg: String, f: () => Unit)
}

object AppCtx {
  val log = Log(this.getClass.getSimpleName)

  var resources: Resources = null

  def init(r: Resources) {
    if (resources != null) throw new IllegalStateException("App context already initialised!")
    resources = r
  }

  lazy val instance: AppCtx = new DroidAppCtx(resources)
  def apply(): AppCtx = instance

}

private class BitmapKey(val path: String, val w: Int, val h: Int) {
  private[this] val hash = Array(path.hashCode, w, h).foldLeft(0)(_ + _ * 67)
  private[this] val str = "(%s,%dx%d)".format(path, w, h)

  override def equals(other: Any): Boolean = other match {
    case bmk: BitmapKey => path.equals(bmk.path) && w == bmk.w && h == bmk.h
    case _ => false
  }
  override def hashCode(): Int = hash
  override def toString: String = str
}

private class DroidAppCtx(resources: Resources) extends AppCtx {

  import AppCtx.log

  private val bgexec = Executors.newFixedThreadPool(1,
    new ThreadFactoryBuilder()
      .setDaemon(true)
      .setNameFormat("bgexec-%d")
      .setPriority(Thread.MIN_PRIORITY)
      .build())

  private val uiexec = new Handler()
  log("ctor").debug("UI executor thread=%s", uiexec.getLooper.getThread.getName)

  private val bmCache: LoadingCache[BitmapKey, Bitmap] = newCacheBuilder()
    .weigher((k: BitmapKey, v: Bitmap) => v.getByteCount)
    .maximumWeight(AppCfg.APP_CTX_BITMAP_CACHE_SIZE_BYTES)
    .build((k: BitmapKey) => {
    if (k.w > 0 && k.h > 0)
      scaleBitmap(BitmapFactory.decodeFile(k.path), k.w, k.h, true)
    else
      BitmapFactory.decodeFile(k.path)
  })

  private val bmdCache: LoadingCache[BitmapKey, BitmapDrawable] = newCacheBuilder()
    .maximumSize(AppCfg.APP_CTX_BITMAP_DRAWALBE_CACHE_MAX_COUNT)
    .build((k: BitmapKey) => new BitmapDrawable(resources, bmCache.get(k)))

  val textEnricher = TextEnricher()

  def loadBitmapThenPost(path: String, w: Int = -1, h: Int = -1, f: (Bitmap) => Unit) {
    enqeueTask("Loading bitmap: " + path, () => {
      val key = new BitmapKey(path, w, h)
      val bm = bmCache.get(key)
      uiexec.post(runnable("Applying bitmap: " + bm, () => f(bm)))
    })
  }

  def loadBitmapDrawableThenPost(path: String, w: Int = -1, h: Int = -1, f: (BitmapDrawable) => Unit) {
    enqeueTask("Loading bitmap: " + path, () => {
      val key = new BitmapKey(path, w, h)
      val bmd = bmdCache.get(key)
      uiexec.post(runnable("Applying bitmap drawable: " + bmd, () => f(bmd)))
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
