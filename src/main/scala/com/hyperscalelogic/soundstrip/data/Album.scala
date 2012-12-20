package com.hyperscalelogic.soundstrip.data

import scala.collection.mutable.{Buffer => MBuffer}

import android.provider.MediaStore.Audio.{AlbumColumns => AlCols, AudioColumns => AuCols, AudioColumns}
import android.provider.{BaseColumns => BaCols}

import android.provider.MediaStore
import com.hyperscalelogic.android.util.db.RichCursor._
import com.hyperscalelogic.android.util.log.Log
import android.content
import content.ContentResolver
import collection.mutable.{Set => MSet, Map => MMap}

class Album(val id: Int,
            val key: Array[Byte],
            val name: String,
            val artist: String,
            val artwork: String)
  extends DataObj(id, key) {
  override def toString: String = "(%s,name=%s,artist=%s,artwork=%s)".format(id, name, artist, artwork)
}

object AlbumStore {
  def apply(resolver: ContentResolver): AlbumStore = new DroidAlbumStore(resolver)
}

trait AlbumStore {
  def foreach(f: (Album) => Unit)
}

private class SyncAlbumStore(other: AlbumStore) extends AlbumStore {
  def foreach(f: (Album) => Unit) {
    this.synchronized(other.foreach(f))
  }
}

private object DroidAlbumStore {
  val log = Log(this.getClass.getSimpleName)
}

class IndexEntry[T, U <: DataObj](field: T, obj: U) {

}

private class DroidAlbumStore(val resolver: ContentResolver) extends AlbumStore {

  import DroidAlbumStore._

  val map = MMap[Int, Album]()

  def loadIds(): Array[Int] = {
    val list = Array.newBuilder[Int]
    val cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
      Array[String](BaCols._ID),
      null,
      null,
      null)
    cursor.foreach(c => list += c.getInt(0))
    list.result()
  }

  def init() {
    val llog = log("init")

    val ids = MSet[Int](loadIds().toSeq: _*)
    map.keySet.foreach(id => ids -= id)

    val cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
      Array[String](BaCols._ID, AlCols.ALBUM_KEY, AlCols.ALBUM, AlCols.ARTIST, AlCols.ALBUM_ART),
      null,
      null,
      null)

    cursor.foreach(c => {
      val album: Album = new Album(c.getInt(0), c.getString(1).getBytes, c.getString(2), c.getString(3), c.getString(4))
      map += (album.id -> album)
      llog.debug("Loaded album: %s", album)
    }
    )
  }

  def foreach(f: (Album) => Unit) {
    val llog = log("loadAlbums")
    init()
    map.foreach(e => f(e._2))
  }

  def trackCount(albumId: Int) {
    val cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      Array[String]("COUNT(%s)".format(BaCols._ID)),
      "%s = ?".format(AuCols.ALBUM_ID),
      Array[String](albumId.toString),
      null)
    cursor.getInt(0)
  }

}

