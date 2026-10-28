package com.quran.labs.androidquran.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.quran.labs.androidquran.ui.helpers.HomeIndexState
import com.quran.labs.androidquran.ui.helpers.HomeTab
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class HomeIndexStateTest {
  @Test fun orderRepairsOldCorruptOrDuplicatePreferencesWithoutLosingAnyTab() {
    assertThat(HomeTab.parseOrder("revelation,unknown,revelation,juz"))
      .containsExactly(HomeTab.REVELATION, HomeTab.JUZ, HomeTab.SURAHS, HomeTab.BOOKMARKS).inOrder()
  }
  @Test fun movingTabsInEitherDirectionPersistsTheirIdentityAndColdDefault() {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val state = HomeIndexState(context)
    state.move(HomeTab.REVELATION, HomeTab.SURAHS)
    assertThat(HomeIndexState(context).order.first()).isEqualTo(HomeTab.REVELATION)
    state.move(HomeTab.REVELATION, HomeTab.BOOKMARKS)
    assertThat(HomeIndexState(context).order)
      .containsExactly(HomeTab.SURAHS, HomeTab.JUZ, HomeTab.BOOKMARKS, HomeTab.REVELATION).inOrder()
    state.move(HomeTab.JUZ, HomeTab.JUZ)
    assertThat(state.order).hasSize(4)
  }
  @Test fun scrollOffsetsAreIndependentAndSurviveANewTaskWithoutRememberingItsSelectedTab() {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val state = HomeIndexState(context)
    state.selected = HomeTab.REVELATION
    state.save(HomeTab.SURAHS, 61, -20)
    state.save(HomeTab.JUZ, 9, -8)
    state.save(HomeTab.REVELATION, 69, -15)
    val restored = HomeIndexState(context)
    assertThat(restored.selected).isNull()
    assertThat(restored.position(HomeTab.SURAHS)).isEqualTo(61)
    assertThat(restored.offset(HomeTab.SURAHS)).isEqualTo(-20)
    assertThat(restored.position(HomeTab.JUZ)).isEqualTo(9)
    assertThat(restored.position(HomeTab.REVELATION)).isEqualTo(69)
  }
}
