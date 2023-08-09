package com.github.ekenstein.sgf2gif

import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLConnection
import kotlin.test.assertEquals

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
}
