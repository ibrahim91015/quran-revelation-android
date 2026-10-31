package com.quran.labs.androidquran.sajdah

import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.data.SajdahLocations

/** The next *recited* ayah follows the session's order, including a surah boundary. */
class SajdahAlertState {
  var sajdah: SuraAyah? = null
    private set
  enum class Change { NONE, SHOW, CLEAR }
  fun completed(ayah: SuraAyah): Change {
    if (SajdahLocations.contains(ayah.sura, ayah.ayah)) {
      sajdah = ayah
      return Change.SHOW
    }
    if (sajdah != null) {
      sajdah = null
      return Change.CLEAR
    }
    return Change.NONE
  }
  fun clear() { sajdah = null }
}
