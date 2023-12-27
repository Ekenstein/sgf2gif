package com.github.ekenstein.sgf2gif

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OptionsTest {
    @Test
    fun `omitting show marker option defaults to false`() {
        val options = Options.parse(emptyArray())
        assertFalse(options.showMarker)
    }

    @Test
    fun `marker flag indicates that it should show marker`() {
        val options = Options.parse(arrayOf("--show-marker"))
        assertTrue(options.showMarker)
    }

    @Test
    fun `omitting loop option defaults to false`() {
        val options = Options.parse(emptyArray())
        assertFalse(options.loop)
    }

    @Test
    fun `loop flag indicates that it should loop the animation`() {
        val options = Options.parse(arrayOf("--loop"))
        assertTrue(options.loop)
    }
}
