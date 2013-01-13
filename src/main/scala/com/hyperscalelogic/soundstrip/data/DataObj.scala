package com.hyperscalelogic.soundstrip.data

import android.provider.MediaStore

abstract class DataObj(id: Id) {
}

trait DataStore {

  protected val ALBUM_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

  protected val TRACK_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

}
