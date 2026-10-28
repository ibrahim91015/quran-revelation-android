package com.quran.labs.androidquran.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.quran.data.page.provider.madani.MadaniPageProvider
import com.quran.labs.androidquran.base.TestApplication
import org.json.JSONArray
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [36])
class ReciterCatalogTest {
  @Test fun `all reference APK reciters retain IDs names formats and timing databases`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val reference = JSONArray(javaClass.getResource("/reference-reciters-3.6.4.json")!!.readText())
    val reciters = MadaniPageProvider().getQaris()
    assertThat(reciters).hasSize(reference.length())
    (0 until reference.length()).forEach { index ->
      val expected = reference.getJSONObject(index)
      val actual = reciters.single { it.id == expected.getInt("id") }
      assertThat(context.resources.getResourceEntryName(actual.nameResource)).isEqualTo(expected.getString("name"))
      assertThat(context.getString(actual.nameResource)).isNotEmpty()
      assertThat(actual.url).isEqualTo(expected.getString("url"))
      assertThat(actual.path).isEqualTo(expected.getString("path"))
      assertThat(actual.opusUrl).isEqualTo(if (expected.isNull("opusUrl")) null else expected.getString("opusUrl"))
      assertThat(actual.db).isEqualTo(if (expected.isNull("db")) null else expected.getString("db"))
      assertThat(actual.hasGaplessAlternative).isEqualTo(expected.getBoolean("hasGaplessAlternative"))
    }
    assertThat(reciters.count { it.isGapless }).isEqualTo(70)
    assertThat(reciters.count { it.hasOpus }).isEqualTo(44)
  }
}
