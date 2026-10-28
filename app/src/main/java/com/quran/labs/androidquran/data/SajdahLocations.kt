package com.quran.labs.androidquran.data

/** The fifteen prostration markers in https://tanzil.net/res/text/metadata/quran-data.xml. */
object SajdahLocations {
  private val locations = setOf(
    7 to 206, 13 to 15, 16 to 50, 17 to 109, 19 to 58,
    22 to 18, 22 to 77, 25 to 60, 27 to 26, 32 to 15,
    38 to 24, 41 to 38, 53 to 62, 84 to 21, 96 to 19
  )

  fun contains(sura: Int, ayah: Int) = sura to ayah in locations
}
