package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.editor.Board
import com.github.ekenstein.sgf.editor.extractBoard
import com.github.ekenstein.sgf.editor.goToRootNode
import com.github.ekenstein.sgf.editor.placeStone
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.stream.ImageOutputStream
import kotlin.time.Duration.Companion.seconds

fun BoardTheme.render(
    outputStream: ImageOutputStream,
    options: Options
) {
    writeGif(outputStream, options.delay.seconds, options.loop) {
        val board = options.sgf.goToRootNode().extractBoard()
        val boardImage = image(options.width, options.height) { g ->
            drawEmptyBoard(g)
            board.stones.forEach { (point, color) ->
                drawStone(g, Stone(point, color))
            }
        }

        addFrame(boardImage)

        tailrec fun addStones(move: Int, board: Board, stones: List<Stone>) {
            val stone = stones.firstOrNull()
            if (stone != null) {
                val updatedBoard = board.placeStone(stone.color, stone.point)
                val capturedStones = board.stones - updatedBoard.stones.keys

                val image = image(options.width, options.height) { g ->
                    drawStone(g, stone)

                    capturedStones.forEach { (point, _) ->
                        clearPoint(g, point.x, point.y)
                    }
                }

                addFrame(image)
                addStones(move + 1, updatedBoard, stones.drop(1))
            }
        }

        val stones = options.sgf.getMoves().reversed()
        addStones(0, board, stones)
    }
}

private fun image(width: Int, height: Int, block: (Graphics2D) -> Unit) =
    BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().apply(block).dispose()
    }
