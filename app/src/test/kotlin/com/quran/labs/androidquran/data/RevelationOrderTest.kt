package com.quran.labs.androidquran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RevelationOrderTest {
  @Test
  fun `traditional order contains every surah exactly once`() {
    assertThat(RevelationOrder.suras.toList()).hasSize(Constants.SURAS_COUNT)
    assertThat(RevelationOrder.suras.toSet()).containsExactlyElementsIn(1..Constants.SURAS_COUNT)
  }

  @Test
  fun `traditional order begins with Al Alaq and ends with An Nasr`() {
    assertThat(RevelationOrder.suras.take(5)).containsExactly(96, 68, 73, 74, 1).inOrder()
    assertThat(RevelationOrder.suras.takeLast(5)).containsExactly(62, 48, 5, 9, 110).inOrder()
  }

  @Test
  fun `position lookup returns zero based revelation position`() {
    assertThat(RevelationOrder.positionOf(96)).isEqualTo(0)
    assertThat(RevelationOrder.positionOf(1)).isEqualTo(4)
    assertThat(RevelationOrder.positionOf(110)).isEqualTo(113)
  }
}
