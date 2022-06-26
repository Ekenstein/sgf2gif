package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import com.github.ekenstein.sgf.editor.SgfEditor
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.stream.ImageOutputStream
import kotlin.time.Duration

data class Stone(
    val point: SgfPoint,
    val color: SgfColor,
    val moveNumber: Int?
)

interface BoardRenderer {
    fun drawEmptyBoard(g: Graphics2D)
    fun drawStone(g: Graphics2D, stone: Stone)
}

fun BoardRenderer.render(
    outputStream: ImageOutputStream,
    editor: SgfEditor,
    width: Int,
    height: Int,
    delay: Duration,
    loop: Boolean
) {
    writeGif(outputStream, delay, loop) {
        add(
            image(width, height) { g ->
                drawEmptyBoard(g)
            }
        )

        editor.getStones().reversed().forEach { stone ->
            add(
                image(width, height) { g ->
                    drawStone(g, stone)
                }
            )
        }
    }
}

private fun image(width: Int, height: Int, block: (Graphics2D) -> Unit) =
    BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().apply(block).dispose()
    }
