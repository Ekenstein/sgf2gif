package com.github.ekenstein.sgf2gif

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLConnection
import kotlin.time.Duration.Companion.seconds

class MainTest {
    @Test
    fun `can read sgf from std-in and write gif to std-out`() {
        val sgf = "(;B[aa];W[bb])"
        val sgfInputStream = ByteArrayInputStream(sgf.toByteArray())

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream, true))
        System.setIn(sgfInputStream)
        main(emptyArray())

        val actualData = outputStream.toByteArray()

        val contentType = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(actualData))
        assertEquals("image/gif", contentType)
    }

    @Test
    fun `can read sgf from file and write gif to std-out`() {
        val sgf = "(;B[aa];W[bb])"
        val tempFile = File.createTempFile("sgf", null)
        tempFile.outputStream().use {
            it.write(sgf.toByteArray())
        }

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream, true))

        main(arrayOf("--file", tempFile.absolutePath))

        val actualData = outputStream.toByteArray()

        val contentType = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(actualData))
        assertEquals("image/gif", contentType)
    }

    @Test
    fun `can read sgf from file and save gif to file`() {
        val sgf = "(;B[aa];W[bb])"
        val tempFile = File.createTempFile("sgf", null)
        tempFile.outputStream().use {
            it.write(sgf.toByteArray())
        }

        val outputFile = File.createTempFile("game", null)
        main(
            arrayOf("--file", tempFile.absolutePath, "--output", outputFile.absolutePath)
        )

        val actualData = outputFile.inputStream().use { it.readAllBytes() }
        val contentType = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(actualData))
        assertEquals("image/gif", contentType)
    }

    @Test
    fun `delay between frames`() {
        val sgf = "(;B[aa];W[bb])"
        val data = generate(sgf, arrayOf("--delay", "0.1")).toByteArray()
        val metadata = GifMetadata.fromInputStream(ByteArrayInputStream(data))
        val expected = 0.1.seconds.inWholeMilliseconds
        assertEquals(expected, metadata.graphicControlExtension.delayTime.inWholeMilliseconds)
    }

    @Test
    fun `a looping animation has the necessary application extensions`() {
        val sgf = "(;B[aa];W[bb])"
        val data = generate(sgf, arrayOf("--loop")).toByteArray()
        val metadata = GifMetadata.fromInputStream(ByteArrayInputStream(data))

        val applicationExtensions = metadata.applicationExtensions.getApplicationExtensions().toList()

        assertEquals(1, applicationExtensions.size)

        assertAll(
            applicationExtensions.map { applicationExtension ->
                {
                    assertEquals("NETSCAPE", applicationExtension.applicationId)
                    assertEquals("2.0", applicationExtension.authenticationCode)
                    val expectedUserObject = byteArrayOf(
                        0x1,
                        (0 and 0xFF).toByte(),
                        (0 shr 8 and 0xFF).toByte()
                    )
                    assertArrayEquals(expectedUserObject, applicationExtension.userObject)
                }
            }
        )
    }

    @Test
    fun `a non looping animation has no application extension`() {
        val sgf = "(;B[aa];W[bb])"
        val data = generate(sgf, emptyArray()).toByteArray()
        val metadata = GifMetadata.fromInputStream(ByteArrayInputStream(data))

        assertEquals(0, metadata.applicationExtensions.getApplicationExtensions().count())
    }

    @Test
    fun `number of frames equals the number of moves plus the empty board frame`() {
        val sgf = "(;B[aa];W[bb])"
        val data = generate(sgf, emptyArray()).toByteArray()
        val gifImage = GifImage.fromByteArray(data)
        assertEquals(3, gifImage.numberOfFrames)
    }

    private fun generate(sgf: String, args: Array<String>): ByteArrayOutputStream {
        val sgfAsBytes = sgf.toByteArray()
        val sgfInputStream = ByteArrayInputStream(sgfAsBytes)

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream, true))
        System.setIn(sgfInputStream)
        main(args)

        return outputStream
    }
}
