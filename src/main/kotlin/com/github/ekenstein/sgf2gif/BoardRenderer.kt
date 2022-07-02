package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import com.github.ekenstein.sgf.editor.Board
import com.github.ekenstein.sgf.editor.SgfEditor
import com.github.ekenstein.sgf.editor.extractBoard
import com.github.ekenstein.sgf.editor.getMoveNumber
import com.github.ekenstein.sgf.editor.goToRootNode
import com.github.ekenstein.sgf.editor.placeStone
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
    fun clearPoint(g: Graphics2D, x: Int, y: Int)
}

fun BoardRenderer.render(
    outputStream: ImageOutputStream,
    editor: SgfEditor,
    width: Int,
    height: Int,
    delay: Duration,
    loop: Boolean,
    removeCapturedStones: Boolean,
    progress: (Double) -> Unit
) {
    writeGif(outputStream, delay, loop) {
        add(
            image(width, height) { g ->
                drawEmptyBoard(g)
            }
        )

        val totalNumberOfMoves = editor.getMoveNumber().toDouble()

        tailrec fun addStones(move: Int, board: Board, stones: List<Stone>) {
            val percentageDone = move / totalNumberOfMoves
            progress(percentageDone)

            val stone = stones.firstOrNull()
            if (stone != null) {
                val updatedBoard = board.placeStone(stone.color, stone.point)
                val capturedStones = board.stones - updatedBoard.stones.keys

                val image = image(width, height) { g ->
                    drawStone(g, stone)

                    if (removeCapturedStones) {
                        capturedStones.forEach { (point, _) ->
                            clearPoint(g, point.x, point.y)
                        }
                    }
                }

                add(image)
                addStones(move + 1, updatedBoard, stones.drop(1))
            }
        }

        val stones = editor.getStones().reversed()
        val board = editor.goToRootNode().extractBoard()
        addStones(0, board, stones)
    }
}

private fun image(width: Int, height: Int, block: (Graphics2D) -> Unit) =
    BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().apply(block).dispose()
    }
