package com.quran.labs.androidquran.ui

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.viewpager.widget.ViewPager
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.base.TestApplication
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(application = TestApplication::class, sdk = [33])
@RunWith(RobolectricTestRunner::class)
class QuranActivityTest {
  @get:Rule
  val rule = ActivityScenarioRule(QuranActivity::class.java)

  @Before
  fun setup() {
    Intents.init()
  }

  @After
  fun teardown() {
    Intents.release()
  }

  @Test
  fun testClickingOnSuraOnListViewNavigatesToSura() {
    //given
    Espresso.onView(ViewMatchers.withText(Matchers.startsWith("Quran")))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    //when
    Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withText("Surah Al-Fātihah"), ViewMatchers.isCompletelyDisplayed()
        )
    ).perform(ViewActions.click())

    //then
    Intents.intended(IntentMatchers.hasComponent(PagerActivity::class.java.name))
    Espresso.onView(ViewMatchers.withText("Surah Al-Fātihah"))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
  }

  @Test
  fun revelationTabStartsWithFirstRevealedSurah() {
    Espresso.onView(ViewMatchers.withText("Revelation"))
      .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    rule.scenario.onActivity { activity ->
      activity.findViewById<ViewPager>(R.id.index_pager).setCurrentItem(2, false)
    }

    Espresso.onView(
      Matchers.allOf(
        ViewMatchers.withText(Matchers.startsWith("Revelation 1")),
        ViewMatchers.isCompletelyDisplayed()
      )
    )
      .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
    Espresso.onView(
      Matchers.allOf(ViewMatchers.withText("96"), ViewMatchers.isCompletelyDisplayed())
    )
      .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
  }
}
