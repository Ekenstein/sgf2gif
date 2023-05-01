package com.github.ekenstein.sgf2gif.themes

import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import com.github.ekenstein.sgf2gif.BoardTheme
import com.github.ekenstein.sgf2gif.Stone
import com.github.ekenstein.sgf2gif.starPoints
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D
import java.lang.Float.max
import java.lang.Float.min

private val COLOR_SHADOW = Color.BLACK
private val COLOR_GOBAN = Color(162, 92, 0, 255)
private val COLOR_BACKGROUND = Color(0, 79, 92, 255)

private const val PIXEL_SIZE = 5F
private const val PILLAR_HEIGHT = 7 * PIXEL_SIZE
private const val BOARD_THICKNESS_FACTOR = 0.13F
private const val PERSPECTIVE_FACTOR = 0.01F
private const val BOARD_SCALE = 0.90F
private const val SHADOW_OFFSET_FACTOR = 0.7F
private const val PLAY_AREA_OFFSET_FACTOR = 0.03F
private const val STONE_WIDTH = 7 * PIXEL_SIZE
private const val STONE_HEIGHT = 8 * PIXEL_SIZE

class Nes(
    private val width: Int,
    private val height: Int,
    private val boardWidth: Int,
    private val boardHeight: Int
) : BoardTheme {
    private val gobanWidth = BOARD_SCALE * width
    private val gobanHeight = BOARD_SCALE * height
    private val gobanThickness = gobanHeight * BOARD_THICKNESS_FACTOR
    private val gobanPerspective = (gobanHeight + gobanThickness) * PERSPECTIVE_FACTOR

    private val playAreaHeight = gobanHeight - gobanThickness - gobanPerspective - PILLAR_HEIGHT
    private val playAreaOffsetX = gobanWidth * PLAY_AREA_OFFSET_FACTOR
    private val playAreaOffsetY = playAreaHeight * PLAY_AREA_OFFSET_FACTOR

    private val gobanStartX = (width / 2) - (gobanWidth / 2)
    private val gobanStartY = (height / 2) - ((gobanHeight - gobanPerspective) / 2)

    private val intersectionHeight = (playAreaHeight - 2 * playAreaOffsetY) / (boardHeight - 1).toFloat()
    private val intersectionWidth = (gobanWidth - 2 * playAreaOffsetX) / (boardWidth - 1).toFloat()

    private val playAreaStartX = gobanStartX + playAreaOffsetX
    private val playAreaStartY = gobanStartY + playAreaOffsetY

    private val stoneWidthPixels = 7F
    private val stoneHeightPixels = 8F

    private val pixelWidth = (intersectionWidth / stoneWidthPixels) * 0.90F
    private val pixelHeight = (intersectionHeight / stoneHeightPixels) * 0.90F

    private val stoneWidth = stoneWidthPixels * pixelWidth
    private val stoneHeight = stoneHeightPixels * pixelHeight

    private fun playAreaX(x: Int): Float {
        return playAreaStartX + intersectionWidth * (x - 1)
    }

    private fun playAreaY(y: Int): Float {
        return playAreaStartY + intersectionHeight * (y - 1)
    }

    override fun drawEmptyBoard(g: Graphics2D) {
        g.color = COLOR_BACKGROUND
        g.fillRect(0, 0, width, height)

        g.color = COLOR_SHADOW
        val shadow = Rectangle2D.Float(
            gobanStartX + (gobanStartX * SHADOW_OFFSET_FACTOR),
            gobanStartY + (gobanStartY * SHADOW_OFFSET_FACTOR),
            gobanWidth,
            gobanHeight
        )
        g.fill(shadow)

        g.color = COLOR_GOBAN
        val playArea = Rectangle2D.Float(
            gobanStartX,
            gobanStartY,
            gobanWidth,
            gobanHeight - gobanThickness - gobanPerspective - PILLAR_HEIGHT
        )
        g.fill(playArea)

        drawBoardThickness(g)
        drawPillars(g)
        drawIntersections(g)
        drawStarPoints(g)
    }

    private fun drawBoardThickness(g: Graphics2D) {
        val startX = gobanStartX
        val startY = gobanStartY + playAreaHeight
        val shadowWidth = (gobanWidth / PIXEL_SIZE).toInt()
        val shadowHeight = (gobanThickness / PIXEL_SIZE).toInt()

        g.color = COLOR_SHADOW
        val shadow = Rectangle2D.Float(startX, startY, shadowWidth * PIXEL_SIZE, shadowHeight * PIXEL_SIZE)
        g.fill(shadow)

        repeat(shadowHeight) { y ->
            val gy = startY + PIXEL_SIZE * y
            val x = if (y % 2 == 0) {
                startX + PIXEL_SIZE
            } else {
                startX
            }
            g.drawShadowedLine(x, gy, shadowWidth)
        }
    }

    private fun drawIntersections(g: Graphics2D) {
        g.stroke = BasicStroke(PIXEL_SIZE)
        g.color = COLOR_SHADOW

        repeat(boardWidth) { x ->
            val gx = playAreaStartX + intersectionWidth * x
            val startY = playAreaStartY
            val endY = playAreaY(boardHeight)
            val line = Line2D.Float(
                gx,
                startY,
                gx,
                endY
            )

            g.draw(line)
        }

        repeat(boardHeight) { y ->
            val gy = playAreaStartY + intersectionHeight * y
            val startX = playAreaStartX
            val endX = playAreaX(boardWidth)
            val line = Line2D.Float(
                startX,
                gy,
                endX,
                gy
            )

            g.draw(line)
        }
    }

    private fun drawStarPoints(g: Graphics2D) {
        if (boardHeight != boardWidth) {
            return
        }

        g.color = COLOR_SHADOW

        val starPoints = starPoints(boardWidth)
        starPoints.forEach { (x, y) ->
            drawStarPoint(g, x, y)
        }
    }

    private fun drawStarPoint(g: Graphics2D, x: Int, y: Int) {
        val gx = playAreaX(x)
        val gy = playAreaY(y)

        val gh = pixelHeight * 3
        val gw = pixelWidth * 4

        val topLeftX = gx - (gw / 2)
        val topLeftY = gy - (gh / 2)

        val rect = Rectangle2D.Float(topLeftX, topLeftY, gw, gh)
        g.fill(rect)
    }

    private fun drawPillars(g: Graphics2D) {
        val pillarWidthInPixels = 15
        val gobanWidthInPixels = (gobanWidth / PIXEL_SIZE).toInt()

        val middleXInPixels = gobanWidthInPixels / 2

        val leftPillarMiddleXInPixels = middleXInPixels - gobanWidthInPixels / 3
        val leftPillarStartXInPixels = leftPillarMiddleXInPixels - (pillarWidthInPixels / 2) + 1

        drawPillar(g, this.gobanStartX + leftPillarStartXInPixels * PIXEL_SIZE)

        val rightPillarMiddleXInPixels = gobanWidthInPixels - leftPillarMiddleXInPixels
        val rightPillarStartXInPixels = rightPillarMiddleXInPixels - (pillarWidthInPixels / 2) + 3

        drawPillar(g, rightPillarStartXInPixels * PIXEL_SIZE)
    }

    private fun drawPillar(g: Graphics2D, startX: Float) {
        val startY = (gobanStartY + playAreaHeight) + (gobanThickness / PIXEL_SIZE).toInt() * PIXEL_SIZE
        g.drawShadowedLine(startX + 2 * PIXEL_SIZE, startY, 11)
        g.drawShadowedLine(startX, startY + PIXEL_SIZE * 2, 15)
        g.drawShadowedLine(startX, startY + PIXEL_SIZE * 4, 15)
        g.drawShadowedLine(startX + 2 * PIXEL_SIZE, startY + PIXEL_SIZE * 6, 11)
    }

    override fun drawStone(g: Graphics2D, stone: Stone) {
        val (x, y) = stone.point
        when (stone.color) {
            SgfColor.Black -> drawBlackStone(g, x, y)
            SgfColor.White -> drawWhiteStone(g, x, y)
        }
    }

    override fun clearPoint(g: Graphics2D, x: Int, y: Int) {
        val gx = playAreaX(x)
        val gy = playAreaY(y)
        val topLeftX = gx - (stoneWidth / 2)
        val topLeftY = gy - (stoneHeight / 2)

        val playAreaStopX = playAreaX(boardWidth)
        val playAreaStopY = playAreaY(boardHeight)

        g.color = COLOR_GOBAN
        val rect = Rectangle2D.Float(
            topLeftX,
            topLeftY,
            stoneWidth,
            stoneHeight
        )

        g.fill(rect)

        val starPoints = if (boardWidth == boardHeight) {
            starPoints(boardWidth)
        } else {
            emptySet()
        }

        if (SgfPoint(x, y) in starPoints) {
            drawStarPoint(g, x, y)
        }

        g.color = COLOR_SHADOW
        g.stroke = BasicStroke(PIXEL_SIZE)

        val l1 = Line2D.Float(
            gx,
            max(topLeftY, playAreaStartY),
            gx,
            min(topLeftY + stoneHeight, playAreaStopY)
        )
        g.draw(l1)

        val l2 = Line2D.Float(
            max(topLeftX, playAreaStartX),
            gy,
            min(topLeftX + stoneWidth, playAreaStopX),
            gy
        )

        g.draw(l2)
    }

    private fun drawWhiteStone(
        g: Graphics2D,
        x: Int,
        y: Int,
    ) {
        val gx = playAreaX(x)
        val gy = playAreaY(y)

        val topLeftX = gx - (stoneWidth / 2)
        val topLeftY = gy - (stoneHeight / 2)

        g.color = Color.WHITE
        val bg = Rectangle2D.Float(
            topLeftX,
            topLeftY,
            stoneWidth,
            stoneHeight
        )
        g.fill(bg)

        g.color = COLOR_GOBAN
        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY,
                2 * pixelWidth,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - 2 * pixelWidth,
                topLeftY,
                2 * pixelWidth,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY + pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - pixelWidth,
                topLeftY + pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY + stoneHeight - pixelHeight,
                pixelWidth * 2,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY + stoneHeight - 2 * pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )
        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - pixelWidth,
                topLeftY + stoneHeight - pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )

        g.color = COLOR_SHADOW
        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - pixelWidth,
                topLeftY + stoneHeight - 3 * pixelHeight,
                pixelWidth,
                pixelHeight * 2
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - 2 * pixelWidth,
                topLeftY + stoneHeight - 2 * pixelHeight,
                pixelWidth * 2,
                pixelHeight
            )
        )
        g.fill(
            Rectangle2D.Float(
                topLeftX + 2 * pixelWidth,
                topLeftY + stoneHeight - pixelHeight,
                pixelWidth * 4,
                pixelHeight
            )
        )
    }

    private fun drawBlackStone(
        g: Graphics2D,
        x: Int,
        y: Int
    ) {
        val gx = playAreaX(x)
        val gy = playAreaY(y)

        val topLeftX = gx - (stoneWidth / 2)
        val topLeftY = gy - (stoneHeight / 2)

        g.color = Color.BLACK
        val bg = Rectangle2D.Float(topLeftX, topLeftY, stoneWidth, stoneHeight)
        g.fill(bg)

        g.color = COLOR_GOBAN

        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY,
                2 * pixelWidth,
                pixelHeight
            )
        )
        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - (2 * pixelWidth),
                topLeftY,
                2 * pixelWidth,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY + pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )
        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - pixelWidth,
                topLeftY + pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY + stoneHeight - pixelHeight,
                2 * pixelWidth,
                pixelHeight
            )
        )
        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - (2 * pixelWidth),
                topLeftY + stoneHeight - pixelHeight,
                2 * pixelWidth,
                pixelHeight
            )
        )

        g.fill(
            Rectangle2D.Float(
                topLeftX,
                topLeftY + stoneHeight - 2 * pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )
        g.fill(
            Rectangle2D.Float(
                topLeftX + stoneWidth - pixelWidth,
                topLeftY + stoneHeight - 2 * pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )

        g.color = Color.WHITE
        g.fill(
            Rectangle2D.Float(
                topLeftX + pixelWidth * 2,
                topLeftY + pixelHeight,
                pixelWidth,
                pixelHeight
            )
        )
        g.fill(
            Rectangle2D.Float(
                topLeftX + pixelWidth,
                topLeftY + 2 * pixelHeight,
                pixelWidth,
                pixelHeight * 3
            )
        )
    }
}

private fun Graphics2D.drawShadowedLine(startX: Float, startY: Float, width: Int) {
    repeat(width) { x ->
        color = if (x % 2 == 0) {
            COLOR_GOBAN
        } else {
            COLOR_SHADOW
        }

        val gx = startX + (PIXEL_SIZE * x)
        val pixel = Rectangle2D.Float(gx, startY, PIXEL_SIZE, PIXEL_SIZE)
        fill(pixel)
    }
}
