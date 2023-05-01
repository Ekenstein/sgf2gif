package com.github.ekenstein.sgf2gif.themes

import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import com.github.ekenstein.sgf2gif.BoardTheme
import com.github.ekenstein.sgf2gif.Stone
import com.github.ekenstein.sgf2gif.starPoints
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.math.min

private const val BOARD_SCALE = 0.95

class Classic(
    private val canvasWidth: Int,
    private val canvasHeight: Int,
    private val boardWidth: Int,
    private val boardHeight: Int
) : BoardTheme {
    private val boardColor = Color.WHITE

    override fun drawEmptyBoard(g: Graphics2D) {
        g.color = boardColor
        g.fillRect(0, 0, canvasWidth, canvasHeight)

        drawIntersections(g)

        if (boardWidth != boardHeight) {
            return
        }

        starPoints(boardWidth).forEach { starPoint ->
            drawStarPoint(g, starPoint)
        }
    }

    override fun drawStone(g: Graphics2D, stone: Stone) {
        val middleX = boardX(stone.point.x - 1, canvasWidth, boardWidth)
        val middleY = boardY(stone.point.y - 1, canvasHeight, boardHeight)

        val circleWidth = (intersectionWidth(canvasWidth, boardWidth) * 0.90).toInt()
        val circleHeight = (intersectionHeight(canvasHeight, boardHeight) * 0.90).toInt()

        val topLeftX = middleX - (circleWidth / 2)
        val topLeftY = middleY - (circleHeight / 2)

        when (stone.color) {
            SgfColor.Black -> {
                g.color = Color.BLACK
                g.fillOval(topLeftX, topLeftY, circleWidth, circleHeight)
            }
            SgfColor.White -> {
                g.color = Color.WHITE
                g.fillOval(topLeftX, topLeftY, circleWidth, circleHeight)

                g.color = Color.BLACK
                g.drawOval(topLeftX, topLeftY, circleWidth, circleHeight)
            }
        }
    }

    override fun clearPoint(g: Graphics2D, x: Int, y: Int) {
        val rectangleWidth = intersectionWidth(canvasWidth, boardWidth)
        val rectangleHeight = intersectionHeight(canvasHeight, boardHeight)
        val middleX = boardX(x - 1, canvasWidth, boardWidth)
        val middleY = boardY(y - 1, canvasHeight, boardHeight)
        val topLeftX = middleX - (rectangleWidth / 2)
        val topLeftY = middleY - (rectangleHeight / 2)

        g.color = boardColor
        g.fillRect(topLeftX, topLeftY, rectangleWidth, rectangleHeight)

        val starPoints = if (boardWidth == boardHeight) {
            starPoints(boardWidth)
        } else {
            emptySet()
        }

        val point = SgfPoint(x, y)
        if (point in starPoints) {
            drawStarPoint(g, point)
        }

        g.color = Color.BLACK
        g.drawLine(
            max(xOffset(canvasWidth), middleX - (rectangleWidth / 2)),
            middleY,
            min(boardHeight(canvasHeight), middleX + (rectangleWidth / 2)),
            middleY
        )

        g.drawLine(
            middleX,
            max(yOffset(canvasHeight), middleY - (rectangleHeight / 2)),
            middleX,
            min(boardWidth(canvasWidth), middleY + (rectangleHeight / 2))
        )
    }

    private fun drawStarPoint(g: Graphics2D, point: SgfPoint) {
        val middleX = boardX(point.x - 1, canvasWidth, boardWidth)
        val middleY = boardY(point.y - 1, canvasHeight, boardHeight)

        val circleWidth = (intersectionWidth(canvasWidth, boardWidth) * 0.20).toInt()
        val circleHeight = (intersectionHeight(canvasHeight, boardHeight) * 0.20).toInt()

        val topLeftX = middleX - (circleWidth / 2)
        val topLeftY = middleY - (circleHeight / 2)

        g.color = Color.BLACK
        g.fillOval(topLeftX, topLeftY, circleWidth, circleHeight)
    }

    private fun drawIntersections(g: Graphics2D) {
        val yOffset = yOffset(canvasHeight)
        val xOffset = xOffset(canvasWidth)

        val intersectionHeight = intersectionHeight(canvasHeight, boardHeight)
        val intersectionWidth = intersectionWidth(canvasWidth, boardWidth)

        g.color = Color.BLACK

        repeat(boardWidth) { x ->
            val gx = boardX(x, canvasWidth, boardWidth)
            g.drawLine(gx, yOffset, gx, yOffset + (intersectionHeight * (boardHeight - 1)))
        }

        repeat(boardHeight) { y ->
            val gy = boardY(y, canvasHeight, boardHeight)
            g.drawLine(xOffset, gy, xOffset + (intersectionWidth * (boardWidth - 1)), gy)
        }
    }
}

private fun intersectionWidth(canvasWidth: Int, boardWidth: Int) = boardWidth(canvasWidth) / boardWidth
private fun intersectionHeight(canvasHeight: Int, boardHeight: Int) = boardHeight(canvasHeight) / boardHeight
private fun boardWidth(canvasWidth: Int) = (BOARD_SCALE * canvasWidth).toInt()
private fun boardHeight(canvasHeight: Int) = (BOARD_SCALE * canvasHeight).toInt()
private fun boardX(x: Int, canvasWidth: Int, boardWidth: Int) =
    (intersectionWidth(canvasWidth, boardWidth) * x) + xOffset(canvasWidth)

private fun boardY(y: Int, canvasHeight: Int, boardHeight: Int) =
    (intersectionHeight(canvasHeight, boardHeight) * y) + yOffset(canvasHeight)

private fun xOffset(canvasWidth: Int) = canvasWidth - boardWidth(canvasWidth)
private fun yOffset(canvasHeight: Int) = canvasHeight - boardHeight(canvasHeight)
