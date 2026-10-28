package com.quran.labs.androidquran.ui

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.viewpager.widget.ViewPager
import com.google.common.truth.Truth.assertThat
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.QuranApplication
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.base.TestApplication
import com.quran.labs.androidquran.common.audio.model.QariItem
import com.quran.labs.androidquran.common.audio.model.playback.*
import com.quran.labs.androidquran.ui.fragment.SuraListFragment
import com.quran.labs.androidquran.ui.helpers.*
import com.quran.labs.androidquran.view.SlidingTabLayout
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import android.os.Looper

@Config(application = TestApplication::class, sdk = [33])
@RunWith(RobolectricTestRunner::class)
class HomeNavigationTest {
  @get:Rule val rule = ActivityScenarioRule(QuranActivity::class.java)
  private fun list(activity: QuranActivity, revelation: Boolean): RecyclerView =
    activity.supportFragmentManager.fragments.filterIsInstance<SuraListFragment>()
      .first { it.isRevelationOrder == revelation }.requireView().findViewById(R.id.recycler_view)
  private fun settle() { shadowOf(Looper.getMainLooper()).idle(); Espresso.onIdle() }
  @Before fun prepare() { settle() }

  @Test fun backgroundResumeKeepsTheReadersOwnScrollInsteadOfJumpingToLatestPage() {
    rule.scenario.onActivity { (list(it, false).layoutManager as LinearLayoutManager).scrollToPositionWithOffset(32, -14) }
    settle()
    var before = -1
    rule.scenario.onActivity { before = (list(it, false).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() }
    rule.scenario.moveToState(Lifecycle.State.CREATED)
    rule.scenario.moveToState(Lifecycle.State.RESUMED)
    settle()
    rule.scenario.onActivity {
      assertThat((list(it, false).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()).isEqualTo(before)
    }
  }

  @Test fun switchingTabsStartsAtTopAndRecreationKeepsTheChosenTabAndOffset() {
    rule.scenario.onActivity { it.findViewById<ViewPager>(R.id.index_pager).setCurrentItem(2, false) }
    settle()
    rule.scenario.onActivity {
      assertThat((list(it, true).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()).isEqualTo(0)
      (list(it, true).layoutManager as LinearLayoutManager).scrollToPositionWithOffset(19, -10)
    }
    settle()
    rule.scenario.recreate()
    settle()
    rule.scenario.onActivity {
      assertThat(it.selectedHomeTab()).isEqualTo(HomeTab.REVELATION)
      assertThat((list(it, true).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()).isEqualTo(19)
      it.findViewById<ViewPager>(R.id.index_pager).setCurrentItem(0, false)
      it.findViewById<ViewPager>(R.id.index_pager).setCurrentItem(2, false)
    }
    settle()
    rule.scenario.onActivity {
      assertThat((list(it, true).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()).isEqualTo(0)
    }
  }

  @Test fun doubleTapAndGreenMarkerApplyOnlyToTheSessionOriginTab() {
    rule.scenario.onActivity { activity ->
      val app = activity.application as QuranApplication
      val ayah = SuraAyah(68, 3)
      val request = AudioRequest(ayah, SuraAyah(110, 3),
        QariItem(0,"Reciter","https://example.com/",path="test",hasGaplessAlternative=false),
        enforceBounds=false, shouldStream=true, audioPathInfo=AudioPathInfo("","",null,emptyList()), revelationOrder=true)
      app.audioStatus.updateAyahPlayback(AudioStatus.Playback(ayah,request,PlaybackStatus.PAUSED))
      activity.findViewById<ViewPager>(R.id.index_pager).setCurrentItem(2, false)
    }
    settle()
    rule.scenario.onActivity { activity ->
      val tabs = activity.findViewById<SlidingTabLayout>(R.id.indicator).getChildAt(0) as ViewGroup
      tabs.getChildAt(2).performClick()
      tabs.getChildAt(2).performClick()
    }
    settle()
    rule.scenario.onActivity {
      assertThat((list(it, true).adapter as QuranListAdapter).isPlayingPosition(1)).isTrue()
      assertThat((list(it, false).adapter as QuranListAdapter).isPlayingPosition(1)).isFalse()
      assertThat((list(it, true).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()).isEqualTo(1)
    }
  }
}
