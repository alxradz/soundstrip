package com.hyperscalelogic.soundstrip.adapter

import android.content.{Context => DroidCtx}
import android.database.DataSetObserver
import android.view.{LayoutInflater, ViewGroup, View}
import com.hyperscalelogic.soundstrip.data.{AlbumStore, TrackStore, Album}
import com.hyperscalelogic.soundstrip.ctx.AppCtx
import android.widget.{ImageView, TextView, ExpandableListAdapter}
import collection.mutable
import com.hyperscalelogic.soundstrip.R
import com.hyperscalelogic.android.util.log.Log

object AlbumExpListAdapter {
  val log = Log(this.getClass.getSimpleName)

  def apply(actx: AppCtx,
            dctx: DroidCtx,
            albumStore: AlbumStore,
            trackStore: TrackStore): AlbumExpListAdapter =
    new AlbumExpListAdapter(actx, dctx, albumStore, trackStore)

}

class AlbumExpListAdapter(private val actx: AppCtx,
                          private val dctx: DroidCtx,
                          private val albumStore: AlbumStore,
                          private val trackStore: TrackStore)
  extends ExpandableListAdapter {

  import AlbumExpListAdapter.log

  val inflater = dctx.getSystemService(DroidCtx.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  private val observers = mutable.Buffer[DataSetObserver]()

  private var albums: List[Album] = init()

  def getGroupView(groupPos: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup): View = {
    val llog = log("getGroupView")
    val view = if (convertView == null) inflater.inflate(R.layout.album_item, parent, false) else convertView
    val album = albums(groupPos)

    llog.debug("Creating view for album: %s", album)

    val nameView: TextView = view.findViewById(R.id.txt_album_name).asInstanceOf[TextView]
    nameView.setText(actx.textEnricher.shadeBrackets(album.name, nameView.getCurrentTextColor, 0.55f))

    view.findViewById(R.id.txt_album_artist).asInstanceOf[TextView].setText(album.artist)
    val artView = view.findViewById(R.id.img_album_art).asInstanceOf[ImageView]

    artView.setImageDrawable(null)
    actx.loadBitmapDrawableThenPost(
      album.artwork,
      artView.getResources.getDimensionPixelSize(R.dimen.album_list_art_width),
      artView.getResources.getDimensionPixelSize(R.dimen.album_list_art_height),
      (bmd) => {
        artView.setImageDrawable(bmd)
      })

    view
  }

  def getChildView(groupPos: Int, childPos: Int, isLastChild: Boolean, convertView: View, parent: ViewGroup): View = {
    val llog = log("getChildView")
    val view = if (convertView == null) inflater.inflate(R.layout.track_item, parent, false) else convertView
    val track = trackStore.findAlbumTracks(albums(groupPos).id)(childPos)

    llog.debug("Creating view for track: %s", track)

    view.findViewById(R.id.txt_track_num).asInstanceOf[TextView].setText(track.trackString)

    val titleView: TextView = view.findViewById(R.id.txt_track_name).asInstanceOf[TextView]
    titleView.setText(actx.textEnricher.shadeBrackets(track.title, titleView.getCurrentTextColor, 0.55f))

    view.findViewById(R.id.txt_track_duration).asInstanceOf[TextView].setText(track.duration.toString)

    view
  }

  def getCombinedChildId(groupId: Long, childId: Long): Long = childId
  def getCombinedGroupId(groupId: Long): Long = groupId

  def registerDataSetObserver(observer: DataSetObserver) {
    observers += observer
  }

  def unregisterDataSetObserver(observer: DataSetObserver) {
    observers -= observer
  }

  def areAllItemsEnabled(): Boolean = true
  def hasStableIds: Boolean = true
  def isEmpty: Boolean = albums.isEmpty
  def getGroupCount: Int = albums.size
  def getGroup(groupPos: Int): AnyRef = albums(groupPos)
  def getGroupId(groupPos: Int): Long = albums(groupPos).id.asLong()
  def isChildSelectable(groupPos: Int, childPos: Int): Boolean = true

  def getChildrenCount(groupPos: Int): Int = {
    trackStore.countAlbumTracks(albums(groupPos).id)
  }

  def getChild(groupPos: Int, childPos: Int): AnyRef = {
    val albumId = albums(groupPos).id
    val tracks = trackStore.findAlbumTracks(albumId)
    tracks(childPos)
  }

  def getChildId(groupPos: Int, childPos: Int): Long = {
    val albumId = albums(groupPos).id
    val tracks = trackStore.findAlbumTracks(albumId)
    tracks(childPos).id.asLong()
  }

  def onGroupCollapsed(groupPos: Int) {}

  def onGroupExpanded(groupPos: Int) {}

  def sortAlbumsByName() {
    albums = albums.sortWith((l, r) => l.name < r.name)
  }

  def sortAlbumsByArtist() {
    albums = albums.sortWith((l, r) => l.artist < r.artist)
  }

  private def init(): List[Album] = {
    val albums = albumStore.findAlbums(albumStore.findAlbumIds())
    //    sortAlbumsByArtist()
    albums
  }
}
