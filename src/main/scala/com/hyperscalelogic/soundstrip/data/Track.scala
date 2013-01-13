package com.hyperscalelogic.soundstrip.data

import org.joda.time.{LocalDateTime, Years}
import android.content.ContentResolver
import android.provider.{BaseColumns => BaCols}
import android.provider.MediaStore.Audio.{AudioColumns => AuCols}
import android.provider.MediaStore.{MediaColumns => MeCols}

import com.hyperscalelogic.android.util.guava.RichCacheBuilder._
import com.hyperscalelogic.android.util.db.RichCursor._
import scala.Array
import com.google.common.cache.LoadingCache
import com.hyperscalelogic.soundstrip.cfg.AppCfg
import java.util.concurrent.TimeUnit

class Track(val id: Id,
            val key: Key[Track],
            val title: String,
            val track: Int,
            val duration: Duration,
            val artist: String,
            val artistId: Long,
            val artistKey: Key[Track],
            val composer: String,
            val album: String,
            val albumId: Long,
            val albumKey: Key[Track],
            val year: Years,
            val mediaType: String,
            val fileName: String,
            val fileSize: Long,
            val fileAdded: LocalDateTime,
            val fileModified: LocalDateTime)
  extends DataObj(id) {

  def trackString: String = {
    val disc = track / 1000
    "% 3d".format(if (disc > 0) track - (disc * 1000) else track)
  }

  override def toString: String = "(%s,track=%d,title=%s,duration=%s,artist=%s,album=%s,mediaType=%s)"
    .format(id, track, title, duration, artist, album, mediaType)
}

object TrackStore {
  def apply(resolver: ContentResolver): TrackStore = new CachedTrackStore(new DroidTrackStore(resolver))
}

trait TrackStore extends DataStore {
  def countAlbumTracks(albumId: Id): Int
  def findAlbumTrackIds(albumId: Id): List[Id]
  def findAlbumTracks(albumId: Id): List[Track]
  def findTrack(trackId: Id): Track
  def foreachTrack(trackIds: List[Id], f: (Track) => Unit)
  def foreachAlbumTrack(albumId: Id, f: (Track) => Unit)
}

private class CachedTrackStore(other: TrackStore) extends TrackStore {

  private val ids: LoadingCache[Id, List[Id]] = newCacheBuilder()
    .maximumSize(AppCfg.CACHED_TRACK_STORE_MAX_IDS_COUNT)
    .expireAfterWrite(AppCfg.CACHED_TRACK_STORE_IDS_EVICT_SECS, TimeUnit.SECONDS)
    .build((id: Id) => other.findAlbumTrackIds(id))

  private val tracks: LoadingCache[Id, Track] = newCacheBuilder()
    .maximumSize(AppCfg.CACHED_TRACK_STORE_MAX_TRACKS_COUNT)
    .expireAfterWrite(AppCfg.CACHED_TRACK_STORE_TRACKS_EVICT_SECS, TimeUnit.SECONDS)
    .build(((id: Id) => other.findTrack(id)))

  def findAlbumTrackIds(albumId: Id): List[Id] = ids.get(albumId)

  def findAlbumTracks(albumId: Id): List[Track] = {
    val b = List.newBuilder[Track]
    foreachAlbumTrack(albumId, t => b += t)
    b.result()
  }

  def foreachAlbumTrack(albumId: Id, f: (Track) => Unit) {
    foreachTrack(findAlbumTrackIds(albumId), f)
  }

  def foreachTrack(trackIds: List[Id], f: (Track) => Unit) {
    //warm up cache
    val notloaded = List.newBuilder[Id]
    trackIds.foreach(id => if (tracks.getIfPresent(id) == null) notloaded += id)
    other.foreachTrack(notloaded.result(), t => tracks.put(t.id, t))

    //execute
    trackIds.foreach(id => f(tracks.get(id)))
  }

  def countAlbumTracks(albumId: Id): Int = findAlbumTrackIds(albumId).size

  def findTrack(trackId: Id): Track = tracks.get(trackId)
}

private class DroidTrackStore(val resolver: ContentResolver) extends TrackStore {

  def findTrack(trackId: Id): Track = {
    var rval: Track = null
    query(
      "%s = ?".format(AuCols.ALBUM_ID),
      Array(trackId.toString),
      AuCols.TRACK,
      t => rval = t)
    if (rval == null) throw new IllegalArgumentException("No track for %s".format(trackId))
    rval
  }

  def countAlbumTracks(albumId: Id): Int = {
    resolver.query(TRACK_URI,
      Array[String](
        "COUNT(%s)".format(BaCols._ID)
      ),
      "%s = ?".format(AuCols.ALBUM_ID),
      Array[String](albumId.toString),
      null).getInt(0)
  }

  def findAlbumTrackIds(albumId: Id): List[Id] = {
    val b = List.newBuilder[Id]
    resolver.query(TRACK_URI,
      Array[String](BaCols._ID),
      "%s = ?".format(AuCols.ALBUM_ID),
      Array[String](albumId.toString),
      AuCols.TRACK).foreach(c => b += Id(c.getLong(0)))
    b.result()
  }

  def findAlbumTracks(albumId: Id): List[Track] = {
    val b = List.newBuilder[Track]
    foreachTrack(findAlbumTrackIds(albumId), t => b += t)
    b.result()
  }

  def foreachAlbumTrack(albumId: Id, f: (Track) => Unit) {
    query(
      "%s = ?".format(AuCols.ALBUM_ID),
      Array(albumId.toString),
      AuCols.TRACK,
      f)
  }

  def foreachTrack(trackIds: List[Id], f: (Track) => Unit) {
    trackIds.grouped(AppCfg.TRACK_STORE_TRACK_BATCH_COUNT).foreach(ids => {
      query(
        "%s in %s".format(BaCols._ID, List.fill(ids.size)("?").mkString("(", ",", ")")),
        ids.map(id => id.toString).toArray,
        AuCols.TRACK,
        f)
    })
  }

  private def query(select: String, selectArgs: Array[String], orderBy: String, f: (Track) => Unit) {
    resolver.query(TRACK_URI,
      Array[String](
        BaCols._ID,
        AuCols.TITLE_KEY,
        MeCols.TITLE,
        AuCols.TRACK,
        AuCols.DURATION,
        AuCols.ARTIST,
        AuCols.ARTIST_ID,
        AuCols.ARTIST_KEY,
        AuCols.COMPOSER,
        AuCols.ALBUM,
        AuCols.ALBUM_ID,
        AuCols.ALBUM_KEY,
        AuCols.YEAR,
        MeCols.MIME_TYPE,
        MeCols.DISPLAY_NAME,
        MeCols.SIZE,
        MeCols.DATE_ADDED,
        MeCols.DATE_MODIFIED
      ),
      select,
      selectArgs,
      orderBy).foreach(c => {
      f(new Track(
        Id(c.getLong(0)),
        Key(c.getString(1)),
        c.getString(2),
        c.getString(3).toInt,
        Duration(c.getLong(4)),
        c.getString(5),
        c.getLong(6),
        Key(c.getString(7).getBytes),
        c.getString(8),
        c.getString(9),
        c.getLong(10),
        Key(c.getString(11).getBytes),
        Years.years(c.getInt(12)),
        c.getString(13),
        c.getString(14),
        c.getLong(15),
        new LocalDateTime(c.getLong(16) * 1000),
        new LocalDateTime(c.getLong(17) * 1000)
      ))
    })

  }

}
