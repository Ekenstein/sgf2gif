package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.Move
import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import com.github.ekenstein.sgf.SgfProperty
import com.github.ekenstein.sgf.editor.SgfEditor
import com.github.ekenstein.sgf.editor.getMoveNumber
import com.github.ekenstein.sgf.editor.goToPreviousNode
import com.github.ekenstein.sgf.utils.MoveResult
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import javax.imageio.stream.ImageOutputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val BOARD_SCALE = 0.95
private fun boardWidth(canvasWidth: Int) = (BOARD_SCALE * canvasWidth).toInt()
private fun boardHeight(canvasHeight: Int) = (BOARD_SCALE * canvasHeight).toInt()

private fun xOffset(canvasWidth: Int) = canvasWidth - boardWidth(canvasWidth)
private fun yOffset(canvasHeight: Int) = canvasHeight - boardHeight(canvasHeight)

private fun intersectionWidth(canvasWidth: Int, boardWidth: Int) = boardWidth(canvasWidth) / boardWidth
private fun intersectionHeight(canvasHeight: Int, boardHeight: Int) = boardHeight(canvasHeight) / boardHeight

private fun boardX(x: Int, canvasWidth: Int, boardWidth: Int) =
    (intersectionWidth(canvasWidth, boardWidth) * x) + xOffset(canvasWidth)

private fun boardY(y: Int, canvasHeight: Int, boardHeight: Int) =
    (intersectionHeight(canvasHeight, boardHeight) * y) + yOffset(canvasHeight)

private fun Graphics2D.drawStone(
    stone: Stone,
    canvasWidth: Int,
    canvasHeight: Int,
    boardWidth: Int,
    boardHeight: Int,
    annotateMoveNumber: Boolean
) {
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val middleX = boardX(stone.point.x - 1, canvasWidth, boardWidth)
    val middleY = boardY(stone.point.y - 1, canvasHeight, boardHeight)

    val circleWidth = (intersectionWidth(canvasWidth, boardWidth) * 0.90)
    val circleHeight = (intersectionHeight(canvasHeight, boardHeight) * 0.90)

    val topLeftX = middleX - (circleWidth / 2)
    val topLeftY = middleY - (circleHeight / 2)

    val shape = Ellipse2D.Double(
        topLeftX,
        topLeftY,
        circleWidth,
        circleHeight
    )

    when (stone.color) {
        SgfColor.Black -> {
            color = Color.BLACK
            fill(shape)

            if (stone.moveNumber != null && annotateMoveNumber) {
                color = Color.WHITE
                drawCenteredText(circleWidth.toInt(), circleHeight.toInt(), middleX, middleY, stone.moveNumber.toString())
            }
        }
        SgfColor.White -> {
            color = Color.WHITE
            fill(shape)

            color = Color.BLACK
            stroke = BasicStroke(3F)
            draw(shape)

            if (stone.moveNumber != null && annotateMoveNumber) {
                drawCenteredText(circleWidth.toInt(), circleHeight.toInt(), middleX, middleY, stone.moveNumber.toString())
            }
        }
    }
}

private fun Graphics.drawStarPoint(
    point: SgfPoint,
    canvasWidth: Int,
    canvasHeight: Int,
    boardWidth: Int,
    boardHeight: Int
) {
    val middleX = boardX(point.x - 1, canvasWidth, boardWidth)
    val middleY = boardY(point.y - 1, canvasHeight, boardHeight)

    val circleWidth = (intersectionWidth(canvasWidth, boardWidth) * 0.20).toInt()
    val circleHeight = (intersectionHeight(canvasHeight, boardHeight) * 0.20).toInt()

    val topLeftX = middleX - (circleWidth / 2)
    val topLeftY = middleY - (circleHeight / 2)

    color = Color.BLACK
    fillOval(topLeftX, topLeftY, circleWidth, circleHeight)
}

private fun Graphics2D.drawIntersections(canvasWidth: Int, canvasHeight: Int, boardWidth: Int, boardHeight: Int) {
    val yOffset = yOffset(canvasHeight)
    val xOffset = xOffset(canvasWidth)

    val intersectionHeight = intersectionHeight(canvasHeight, boardHeight)
    val intersectionWidth = intersectionWidth(canvasWidth, boardWidth)

    color = Color.BLACK

    repeat(boardWidth) { x ->
        stroke = if (x == 0 || x == boardWidth - 1) {
            BasicStroke(3F)
        } else {
            BasicStroke(1F)
        }
        val gx = boardX(x, canvasWidth, boardWidth)
        drawLine(gx, yOffset, gx, yOffset + (intersectionHeight * (boardHeight - 1)))
    }

    repeat(boardHeight) { y ->
        stroke = if (y == 0 || y == boardHeight - 1) {
            BasicStroke(3F)
        } else {
            BasicStroke(1F)
        }
        val gy = boardY(y, canvasHeight, boardHeight)
        drawLine(xOffset, gy, xOffset + (intersectionWidth * (boardWidth - 1)), gy)
    }
}

private fun Graphics2D.drawEmptyBoard(canvasWidth: Int, canvasHeight: Int, boardWidth: Int, boardHeight: Int) {
    color = Color.WHITE
    fillRect(0, 0, canvasWidth, canvasHeight)

    drawIntersections(canvasWidth, canvasHeight, boardWidth, boardHeight)

    if (boardWidth != boardHeight) {
        return
    }

    starPoints(boardWidth).forEach { starPoint ->
        drawStarPoint(starPoint, canvasWidth, canvasHeight, boardWidth, boardHeight)
    }
}

private fun Graphics.drawCenteredText(width: Int, height: Int, x: Int, y: Int, string: String) {
    font = findFont(Dimension(width, height), font, string)
    val bounds = fontMetrics.getStringBounds(string, this)
    val textHeight = bounds.height
    val textWidth = bounds.width

    val cornerX = x - (textWidth / 2)
    val cornerY = y - (textHeight / 2) + fontMetrics.ascent

    drawString(string, cornerX.toInt(), cornerY.toInt())
}

fun Graphics.getFontSize(font: Font, text: String): Dimension {
    val metrics = getFontMetrics(font)
    val hgt = metrics.height
    val adv = metrics.stringWidth(text)
    val padding = 10
    return Dimension(adv + padding, hgt + padding)
}

fun Graphics.findFont(componentSize: Dimension, oldFont: Font, text: String): Font {
    tailrec fun find(savedFont: Font, size: Int): Font {
        val font = Font(oldFont.fontName, oldFont.style, size)
        val d = getFontSize(font, text)
        return if (componentSize.height < d.height || componentSize.width < d.width) {
            savedFont
        } else {
            find(font, size + 1)
        }
    }

    return find(oldFont, 0)
}

private data class Stone(
    val point: SgfPoint,
    val color: SgfColor,
    val moveNumber: Int?
)

private fun SgfEditor.getStones(): List<Stone> {
    tailrec fun SgfEditor.next(result: List<Stone>): List<Stone> {
        val stones = currentNode.properties.flatMap { property ->
            when (property) {
                is SgfProperty.Setup.AB -> property.points.map { Stone(it, SgfColor.Black, null) }
                is SgfProperty.Setup.AW -> property.points.map { Stone(it, SgfColor.White, null) }
                is SgfProperty.Move.B -> when (val move = property.move) {
                    Move.Pass -> emptyList()
                    is Move.Stone -> listOf(Stone(move.point, SgfColor.Black, getMoveNumber()))
                }
                is SgfProperty.Move.W -> when (val move = property.move) {
                    Move.Pass -> emptyList()
                    is Move.Stone -> listOf(Stone(move.point, SgfColor.White, getMoveNumber()))
                }
                else -> emptyList()
            }
        }

        val allStones = result + stones

        return when (val next = goToPreviousNode()) {
            is MoveResult.Failure -> allStones
            is MoveResult.Success -> next.position.next(allStones)
        }
    }

    return next(emptyList())
}

private fun boardImage(canvasWidth: Int, canvasHeight: Int, boardWidth: Int, boardHeight: Int): RenderedImage =
    BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            drawEmptyBoard(width, height, boardWidth, boardHeight)
        }.dispose()
    }

private fun stoneImage(
    stone: Stone,
    canvasWidth: Int,
    canvasHeight: Int,
    boardWidth: Int,
    boardHeight: Int,
    annotateMoveNumber: Boolean
): RenderedImage =
    BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            drawStone(stone, width, height, boardWidth, boardHeight, annotateMoveNumber)
        }.dispose()
    }

fun convertPositionToImage(
    position: SgfEditor,
    width: Int = 1000,
    height: Int = 1000
): RenderedImage {
    val (boardWidth, boardHeight) = position.boardSize()
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        drawEmptyBoard(width, height, boardWidth, boardHeight)

        position.getStones().forEach {
            drawStone(it, width, height, boardWidth, boardHeight, true)
        }
    }

    graphics.dispose()
    return image
}

fun convertPositionToAnimatedGif(
    outputStream: ImageOutputStream,
    editor: SgfEditor,
    width: Int = 1000,
    height: Int = 1000,
    delay: Duration = 2.seconds,
    loop: Boolean = true,
    showMoveNumber: Boolean = true
) {
    val (boardWidth, boardHeight) = editor.boardSize()
    writeGif(outputStream, delay, loop) {
        add(boardImage(width, height, boardWidth, boardHeight))

        editor.getStones().reversed().forEach {
            val image = stoneImage(it, width, height, boardWidth, boardHeight, showMoveNumber)
            add(image)
        }
    }
}
