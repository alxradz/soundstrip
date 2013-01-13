package com.hyperscalelogic.soundstrip.data

import android.provider.MediaStore.Audio.{AlbumColumns => AlCols, AudioColumns => AuCols}
import android.provider.{BaseColumns => BaCols}

import com.hyperscalelogic.android.util.db.RichCursor._
import com.hyperscalelogic.soundstrip.cfg.AppCfg
import com.google.common.cache.LoadingCache
import com.hyperscalelogic.android.util.guava.RichCacheBuilder._
import java.util.concurrent.TimeUnit
import android.content.ContentResolver

class Album(val id: Id,
            val key: Key[Album],
            val name: String,
            val artist: String,
            val artwork: String)
  extends DataObj(id) {
  override def toString: String = "(%s,name=%s,artist=%s,artwork=%s)".format(id, name, artist, artwork)
}

object AlbumStore {
  def apply(resolver: ContentResolver): AlbumStore =
    new DroidAlbumStore(resolver)
}

trait AlbumStore extends DataStore {
  def findAlbumIds(): List[Id]
  def findAlbums(ids: List[Id]): List[Album]
  def findAlbum(id: Id): Album
  def foreachAlbum(ids: List[Id], f: (Album) => Unit)
}

private class CachedAlbumStore(other: AlbumStore) extends AlbumStore {

  private val KEY = Id(-1000)

  private val ids: LoadingCache[Id, List[Id]] = newCacheBuilder()
    .maximumSize(AppCfg.CACHED_ALBUM_STORE_MAX_IDS_COUNT)
    .expireAfterWrite(AppCfg.CACHED_ALBUM_STORE_IDS_EVICT_SECS, TimeUnit.SECONDS)
    .build((id: Id) => other.findAlbumIds())

  private val albums: LoadingCache[Id, Album] = newCacheBuilder()
    .maximumSize(AppCfg.CACHED_ALBUM_STORE_MAX_ALBUMS_COUNT)
    .expireAfterWrite(AppCfg.CACHED_ALBUM_STORE_ALBUMS_EVICT_SECS, TimeUnit.SECONDS)
    .build(((id: Id) => other.findAlbum(id)))

  def findAlbumIds(): List[Id] = ids.get(KEY)

  def findAlbums(ids: List[Id]): List[Album] = {
    val b = List.newBuilder[Album]
    foreachAlbum(ids, a => b += a)
    b.result()
  }

  def findAlbum(id: Id): Album = albums.get(id)

  def foreachAlbum(albumIds: List[Id], f: (Album) => Unit) {
    //warm up cache
    val notloaded = List.newBuilder[Id]
    albumIds.foreach(id => if (albums.getIfPresent(id) == null) notloaded += id)
    other.foreachAlbum(notloaded.result(), t => albums.put(t.id, t))

    //execute
    albumIds.foreach(id => f(albums.get(id)))
  }
}

private class DroidAlbumStore(private val resolver: ContentResolver) extends AlbumStore {

  def findAlbumIds(): List[Id] = {
    val b = List.newBuilder[Id]
    resolver.query(ALBUM_URI,
      Array[String](BaCols._ID),
      null,
      null,
      null).foreach(c => b += Id(c.getLong(0)))
    b.result()
  }

  def findAlbum(albumId: Id): Album = {
    var rval: Album = null
    foreachAlbum(List[Id](albumId), a => rval = a)
    if (rval == null) throw new IllegalArgumentException("No album for ID: %s".format(albumId))
    rval
  }

  def findAlbums(ids: List[Id]): List[Album] = {
    val lb = List.newBuilder[Album]
    foreachAlbum(ids, lb += _)
    lb.result()
  }

  def foreachAlbum(ids: List[Id], f: (Album) => Unit) {
    ids.grouped(AppCfg.ALBUM_STORE_ALBUM_BATCH_COUNT).foreach(batch => {
      query(
        "%s in %s".format(BaCols._ID, List.fill(batch.size)("?").mkString("(", ",", ")")),
        batch.map(id => id.toString).toArray,
        null,
        f)
    })
  }

  private def query(select: String, selectArgs: Array[String], orderBy: String, f: (Album) => Unit) {
    resolver.query(ALBUM_URI,
      Array[String](
        BaCols._ID,
        AlCols.ALBUM_KEY,
        AlCols.ALBUM,
        AlCols.ARTIST,
        AlCols.ALBUM_ART
      ),
      select,
      selectArgs,
      orderBy).foreach(c => {
      f(new Album(
        Id(c.getLong(0)),
        Key(c.getString(1).getBytes),
        c.getString(2),
        c.getString(3),
        c.getString(4)))
    })
  }
}

