package com.hyperscalelogic.soundstrip.activity

import android.app.ExpandableListActivity
import android.os.Bundle
import android.provider.BaseColumns._
import android.provider.MediaStore.Audio.AlbumColumns._
import com.hyperscalelogic.soundstrip.adapter.AlbumExpListAdapter
import com.hyperscalelogic.soundstrip.data.{TrackStore, AlbumStore}
import com.hyperscalelogic.soundstrip.ctx.AppCtx
import com.hyperscalelogic.android.util.log.Log
import com.hyperscalelogic.soundstrip.R

object AlbumListActivity {
  val log = Log(this.getClass.getSimpleName)
}

class AlbumListActivity extends ExpandableListActivity {

  import AlbumListActivity._

  val projection = Array[String](_ID, ALBUM_KEY, ALBUM, ARTIST, ALBUM_ART)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    log("onCreate").debug("Show album list!!")

    setContentView(R.layout.exp_list_view)

//    getExpandableListView.setDrawingCacheEnabled(true)
//    getExpandableListView.setScrollingCacheEnabled(true)

    setListAdapter(AlbumExpListAdapter(AppCtx.instance,
      this,
      AlbumStore(getContentResolver),
      TrackStore(getContentResolver)))
  }

}
