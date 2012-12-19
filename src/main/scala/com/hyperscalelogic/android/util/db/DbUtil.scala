package com.hyperscalelogic.android.util.db

import android.database.Cursor
import android.database.Cursor._
import com.hyperscalelogic.android.util.log.Printer

object DbUtil {

  def dumpCursor(out: Printer, cursor: Cursor, count: Int = Int.MaxValue) {
    val lastPos = cursor.getPosition
    var i = 1
    cursor.moveToFirst()
    while (!cursor.isAfterLast && i < count) {
      var row = List[String]()
      for (idx <- 0 until cursor.getColumnCount) {
        val typeCode = cursor.getType(idx)
        val name = cursor.getColumnName(idx)
        var typeStr = ""

        var value: Any = null
        typeCode match {
          case FIELD_TYPE_BLOB => value = cursor.getBlob(idx); typeStr = "BLOB"
          case FIELD_TYPE_FLOAT => value = cursor.getFloat(idx); typeStr = "FLOAT"
          case FIELD_TYPE_INTEGER => value = cursor.getInt(idx); typeStr = "INT"
          case FIELD_TYPE_NULL => value = null; typeStr = "NULL"
          case FIELD_TYPE_STRING => value = cursor.getString(idx); typeStr = "STRING"
        }

        row = "%s:%s=%s".format(name, typeStr, value) :: row
        i += 1
      }
      out.debug(row.mkString(", "))
      cursor.moveToNext()
    }
    cursor.moveToPosition(lastPos)
  }

  def foreach(cursor: Cursor, f: (Cursor => Unit)) {
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
      f(cursor)
      cursor.moveToNext()
    }
  }

  def inrange(cursor: Cursor, range: Range = Range(0, Int.MaxValue), f: (Cursor => Unit)) {
    cursor.moveToPosition(range.start)
    while (!cursor.isAfterLast && range.contains(cursor.getPosition)) {
      f(cursor)
      cursor.moveToNext()
    }
  }

}
