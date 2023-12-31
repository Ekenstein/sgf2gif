package com.github.ekenstein.sgf2gif

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

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

    @Test
    fun `omitting delay will default to 2 seconds`() {
        val options = Options.parse(emptyArray())
        assertEquals(2.0.seconds, options.delay)
    }

    @Test
    fun `can express delay as a double`() {
        val options = Options.parse(arrayOf("--delay", "0.1"))
        assertEquals(0.1.seconds, options.delay)
    }

    @Test
    fun `can express delay as an integer`() {
        val options = Options.parse(arrayOf("--delay", "1"))
        assertEquals(1.seconds, options.delay)
    }
}
