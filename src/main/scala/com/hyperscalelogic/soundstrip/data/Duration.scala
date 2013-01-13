package com.hyperscalelogic.soundstrip.data

object Duration {
  def apply(d: Long) = new Duration(d)
}

class Duration(val duration: Long) {
  override def equals(o: Any): Boolean = if (o.isInstanceOf[Duration]) duration.equals(o.asInstanceOf[Duration].duration) else false
  override def hashCode(): Int = 47 * duration.hashCode()
  override def toString: String = {
    val hours = duration / (60 * 60 * 1000)
    val mins = (duration - (hours * 60 * 60 * 1000)) / (60 * 1000)
    val secs = (duration - (hours * 60 * 60 * 1000 + mins * 60 * 1000)) / 1000
    if (hours > 0) "% 2d:%02d:%02d".format(hours, mins, secs) else "   % 2d:%02d".format(mins, secs)
  }
}
