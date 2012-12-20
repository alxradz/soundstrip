package com.hyperscalelogic.soundstrip.activity

import android.app.ListActivity
import android.os.Bundle
import android.provider.BaseColumns._
import android.provider.MediaStore.Audio.AlbumColumns._
import com.hyperscalelogic.soundstrip.adapter.AlbumListAdapter
import com.hyperscalelogic.soundstrip.R
import com.hyperscalelogic.soundstrip.data.AlbumStore
import com.hyperscalelogic.soundstrip.ctx.AppCtx
import com.hyperscalelogic.android.util.log.Log

object AlbumListActivity extends ListActivity {
  val log = Log(this.getClass.getSimpleName)
}

class AlbumListActivity extends ListActivity {

  import AlbumListActivity._

  val projection = Array[String](_ID, ALBUM_KEY, ALBUM, ARTIST, ALBUM_ART)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    log("onCreate").debug("Show album list!!")

    val adapter = AlbumListAdapter(AppCtx.instance, this, R.layout.album_item)

    setListAdapter(adapter)

    AlbumStore(getContentResolver).foreach(album => adapter.add(album))
  }

}
