package com.quran.labs.androidquran.ui.helpers

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quran.labs.androidquran.QuranApplication
import com.quran.labs.androidquran.common.audio.model.playback.AudioStatus
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle

/** Owns one tab's offset; repeated RecyclerView IDs never restore another tab's hierarchy state. */
class IndexScrollController(
  private val recycler: RecyclerView, owner: LifecycleOwner, val tab: HomeTab
) : DefaultLifecycleObserver {
  private val app = recycler.context.applicationContext as QuranApplication
  private val state = app.homeIndexState
  private var pending: Pair<Int, Int>? = if (state.pendingTop.remove(tab)) 0 to 0 else state.position(tab) to state.offset(tab)
  private val adapter get() = recycler.adapter as QuranListAdapter
  private val layout get() = recycler.layoutManager as LinearLayoutManager
  private val observer = object : RecyclerView.AdapterDataObserver() {
    override fun onChanged() { restore(); highlight() }
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { restore(); highlight() }
  }
  private val scrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(view: RecyclerView, newState: Int) {
      if (newState == RecyclerView.SCROLL_STATE_IDLE) save()
    }
  }
  init {
    recycler.isSaveEnabled = false
    recycler.isSaveFromParentEnabled = false
    state.controllers[tab] = this
    owner.lifecycle.addObserver(this)
    adapter.registerAdapterDataObserver(observer)
    recycler.addOnScrollListener(scrollListener)
    restore()
    owner.lifecycleScope.launch {
      owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        app.audioStatus.audioPlaybackFlow.collect { highlight() }
      }
    }
  }
  private fun restore() {
    val target = pending ?: return
    if (adapter.itemCount == 0) return
    pending = null
    layout.scrollToPositionWithOffset(target.first.coerceAtMost(adapter.itemCount - 1), target.second)
  }
  private fun highlight() {
    val playing = app.audioStatus.audioPlaybackFlow.value as? AudioStatus.Playback
    adapter.setPlayingPosition(if (playing?.audioRequest?.sourceTab == tab.key) {
      adapter.positionForPlayback(playing.currentAyah, tab, app.quranInfo.getPageFromSuraAyah(
        playing.currentAyah.sura, playing.currentAyah.ayah))
    } else -1)
  }
  fun top() { scroll(0, 0) }
  fun playing() {
    val playing = app.audioStatus.audioPlaybackFlow.value as? AudioStatus.Playback ?: return
    if (playing.audioRequest.sourceTab != tab.key) return
    val index = adapter.positionForPlayback(playing.currentAyah, tab,
      app.quranInfo.getPageFromSuraAyah(playing.currentAyah.sura, playing.currentAyah.ayah))
    if (index >= 0) scroll(index, 0)
  }
  private fun scroll(position: Int, offset: Int) {
    recycler.stopScroll()
    pending = position to offset
    state.save(tab, position, offset)
    restore()
  }
  fun save() {
    if (pending != null || recycler.isLayoutRequested) return
    val first = layout.findFirstVisibleItemPosition()
    state.save(tab, first, layout.findViewByPosition(first)?.let { layout.getDecoratedTop(it) - recycler.paddingTop } ?: 0)
  }
  override fun onStart(owner: LifecycleOwner) {
    // A previously covered picker can become visible again after another picker
    // finishes. Reclaim its controllers and the most recently saved tab offsets.
    state.controllers[tab] = this
    pending = if (state.pendingTop.remove(tab)) 0 to 0 else state.position(tab) to state.offset(tab)
    restore()
    highlight()
  }
  override fun onStop(owner: LifecycleOwner) { save() }
  override fun onDestroy(owner: LifecycleOwner) {
    save()
    adapter.unregisterAdapterDataObserver(observer)
    recycler.removeOnScrollListener(scrollListener)
    if (state.controllers[tab] === this) state.controllers.remove(tab)
  }
}
