package com.hyperscalelogic.soundstrip.activity

import android.app.Activity
import android.os.{Handler, Bundle}
import android.widget.TextView

import com.hyperscalelogic.soundstrip.data._
import com.hyperscalelogic.soundstrip.R
import android.view.View
import android.content.Intent
import com.hyperscalelogic.soundstrip.ctx.AppCtx
import com.hyperscalelogic.android.util.log.Log

object MainActivity extends Activity {
  Log.init("soundstrip")

  val log = Log(this.getClass.getSimpleName)

  try {
    AppCtx.init(new Handler())
  } catch {
    case t: Throwable => log("static ctor").error(t, "Failed to create UI executor")
  }
}

class MainActivity extends Activity {

  import MainActivity._

  var helloTextView: Option[TextView] = None

  var albumStore: AlbumStore = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    log("onCreate").debug("HELLO WORLD!")
    setContentView(R.layout.main)
  }

  override def onResume() {
    super.onResume()
    val llog = log("onResume").debug("")
  }

  override def onRestart() {
    super.onRestart()
  }
  override def onStart() {
    super.onStart()
  }
  override def onStop() {
    super.onStop()
  }
  override def onDestroy() {
    super.onDestroy()
  }
  override def onPause() {
    super.onPause()
  }
  def showAlbums(v: View) {
    startActivity(new Intent(this, classOf[AlbumListActivity]))
  }
}
