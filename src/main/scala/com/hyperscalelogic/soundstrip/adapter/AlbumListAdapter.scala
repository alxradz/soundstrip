package com.hyperscalelogic.soundstrip.adapter

import android.content.{Context => DroidCtx}
import android.view.{ViewGroup, View, LayoutInflater}
import gnu.trove.list.{TLongList, TIntList}
import gnu.trove.list.array.{TLongArrayList, TIntArrayList}
import gnu.trove.map.{TLongObjectMap, TIntObjectMap}
import gnu.trove.map.hash.{TLongObjectHashMap, TIntObjectHashMap}
import collection.mutable.{Buffer => MBuffer}
import com.hyperscalelogic.soundstrip.R
import com.hyperscalelogic.soundstrip.data.Album
import android.database.DataSetObserver
import android.widget.{ImageView, TextView}
import com.hyperscalelogic.soundstrip.ctx.AppCtx
import com.hyperscalelogic.android.util.log.Log

object AlbumListAdapter {
  val log = Log(this.getClass.getSimpleName)

  def apply(actx: AppCtx, dctx: DroidCtx, resource: Int): ModListAdapter[Album] =
    new SynchListAdapter[Album](new AlbumListAdapter(actx, dctx, resource))
}

private class AlbumListAdapter(actx: AppCtx, dctx: DroidCtx, resource: Int) extends ModListAdapter[Album] {

  import AlbumListAdapter.log

  val inflater = dctx.getSystemService(DroidCtx.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  val list: TLongList = new TLongArrayList()
  val map: TLongObjectMap[Album] = new TLongObjectHashMap[Album]()
  val observers: MBuffer[DataSetObserver] = MBuffer()

  def add(elems: Seq[Album]) {
    elems.foreach(elem => add(elem, notify = false))
    observers.foreach(_.onChanged())
  }

  def add(elem: Album, notify: Boolean = true) {
//    list.add(elem.id)
//    map.put(elem.id, elem)
    if (notify) observers.foreach(_.onChanged())
  }

  def getCount: Int = list.size()
  def getItem(position: Int): AnyRef = map.get(list.get(position))
  def getItemId(position: Int): Long = list.get(position)
  def areAllItemsEnabled(): Boolean = true
  def isEnabled(position: Int): Boolean = true
  def getItemViewType(position: Int): Int = 0
  def getViewTypeCount: Int = 1
  def hasStableIds: Boolean = true
  def isEmpty: Boolean = list.isEmpty

  def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val llog = log("getView")
    val view = if (convertView == null) inflater.inflate(resource, parent, false) else convertView
    val album = map.get(list.get(position))

    llog.debug("Creating adapter for album: %s", album)

    val nameView: TextView = view.findViewById(R.id.txt_album_name).asInstanceOf[TextView]
    nameView.setText(actx.textEnricher.shadeBrackets(album.name, nameView.getCurrentTextColor, 0.55f))

    view.findViewById(R.id.txt_album_artist).asInstanceOf[TextView].setText(album.artist)
    val artView = view.findViewById(R.id.img_album_art).asInstanceOf[ImageView]

    actx.loadBitmapThenPost(
      album.artwork,
      artView.getResources.getDimensionPixelSize(R.dimen.album_list_art_width),
      artView.getResources.getDimensionPixelSize(R.dimen.album_list_art_height),
      (bitmap) => artView.setImageBitmap(bitmap))

    view
  }

  def registerDataSetObserver(observer: DataSetObserver) {
    observers += observer
  }

  def unregisterDataSetObserver(observer: DataSetObserver) {
    observers -= observer
  }

}
