package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.editor.SgfEditor
import com.github.ekenstein.sgf.editor.commit
import com.github.ekenstein.sgf.editor.placeStone
import com.github.ekenstein.sgf.serialization.encodeToString
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.PrintStream

class GenerationTest {
    private val smallSgf = SgfEditor()
        .placeStone(SgfColor.Black, 3, 3)
        .placeStone(SgfColor.White, 16, 3)
        .placeStone(SgfColor.Black, 16, 16)
        .commit()
        .encodeToString()

    @Test
    fun `generate NES themed gif`() {
        val data = generate(arrayOf("--theme", "NES", "--show-marker", "--loop", "--width", "1000", "--height", "1000"))
        val file = FileOutputStream("C:\\temp\\nes.gif")

        data.writeTo(file)
    }

    @Test
    fun `generate classic themed gif`() {
        val data = generate(arrayOf("--theme", "classic", "--show-marker", "--delay", "0.1"))
        val file = FileOutputStream("C:\\temp\\classic.gif")

        data.writeTo(file)
    }

    private fun generate(args: Array<String>): ByteArrayOutputStream {
        val sgfInputStream = ByteArrayInputStream(smallSgf.toByteArray())

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream, true))
        System.setIn(sgfInputStream)
        main(args)

        return outputStream
    }
}
