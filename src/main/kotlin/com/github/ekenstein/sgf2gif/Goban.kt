package com.github.ekenstein.sgf2gif

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D

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

private fun gobanHeight(height: Int) = BOARD_SCALE * height
private fun gobanWidth(width: Int) = BOARD_SCALE * width
private fun gobanThickness(height: Int) = gobanHeight(height) * BOARD_THICKNESS_FACTOR
private fun gobanPerspective(height: Int): Float {
    val actualHeight = gobanHeight(height) + gobanThickness(height)
    return actualHeight * PERSPECTIVE_FACTOR
}

private fun playAreaHeight(height: Int): Float {
    val gobanHeight = gobanHeight(height)
    val boardThickness = gobanThickness(height)
    val perspective = gobanPerspective(height)
    return gobanHeight - boardThickness - perspective - PILLAR_HEIGHT
}

private fun playAreaWidth(width: Int): Float = gobanWidth(width)

private fun playAreaStartX(width: Int): Float {
    val offset = playAreaOffsetX(width)
    return gobanStartX(width) + offset
}

private fun playAreaStartY(height: Int): Float {
    val offset = playAreaOffsetY(height)
    return gobanStartY(height) + offset
}

private fun gobanShadowX(width: Int): Float {
    val gobanX = gobanStartX(width)
    val offset = gobanX * SHADOW_OFFSET_FACTOR
    return gobanX + offset
}

private fun gobanShadowY(height: Int): Float {
    val gobanY = gobanStartY(height)
    val offset = gobanY * SHADOW_OFFSET_FACTOR
    return gobanY + offset
}

private fun gobanStartX(width: Int): Float {
    val middleX = width / 2
    val boardWidth = gobanWidth(width)

    return middleX - (boardWidth / 2)
}

private fun gobanStartY(height: Int): Float {
    val middleY = height / 2
    val boardHeight = (gobanHeight(height) - gobanPerspective(height)) / 2

    return middleY - boardHeight
}

private fun boardThicknessStartY(height: Int): Float {
    return gobanStartY(height) + playAreaHeight(height)
}

fun Graphics2D.drawGoban(height: Int, width: Int, boardWidth: Int, boardHeight: Int) {
    color = COLOR_BACKGROUND
    fillRect(0, 0, width, height)

    color = COLOR_SHADOW
    val shadow = Rectangle2D.Float(
        gobanShadowX(width),
        gobanShadowY(height),
        gobanWidth(width),
        gobanHeight(height)
    )
    fill(shadow)

    color = COLOR_GOBAN
    val playArea = Rectangle2D.Float(
        gobanStartX(width),
        gobanStartY(height),
        gobanWidth(width),
        gobanHeight(height) - gobanThickness(height) - gobanPerspective(height) - PILLAR_HEIGHT
    )
    fill(playArea)

    drawBoardThickness(width, height)
    drawPillars(width, height)
    drawIntersections(width, height, boardWidth, boardHeight)
    drawStarPoints(width, height, boardWidth, boardHeight)
}

private fun Graphics2D.drawStarPoints(width: Int, height: Int, boardWidth: Int, boardHeight: Int) {
    if (boardHeight != boardWidth) {
        return
    }

    color = COLOR_SHADOW

    val starPoints = starPoints(boardWidth)
    starPoints.forEach { (x, y) ->
        val gx = playAreaX(x, width, boardWidth)
        val gy = playAreaY(y, height, boardHeight)

        val gh = PIXEL_SIZE * 3
        val gw = PIXEL_SIZE * 4

        val topLeftX = gx - (gw / 2)
        val topLeftY = gy - (gh / 2)

        val rect = Rectangle2D.Float(topLeftX, topLeftY, gw, gh)
        fill(rect)
    }
}

fun Graphics2D.drawWhiteStone(x: Int, y: Int, width: Int, height: Int, boardWidth: Int, boardHeight: Int) {
    val gx = playAreaX(x, width, boardWidth)
    val gy = playAreaY(y, height, boardHeight)

    val stoneWidth = 7 * PIXEL_SIZE
    val stoneHeight = 8 * PIXEL_SIZE

    val topLeftX = gx - (stoneWidth / 2)
    val topLeftY = gy - (stoneHeight / 2)

    color = Color.BLACK
    fill(Rectangle2D.Float(topLeftX + 2 * PIXEL_SIZE, topLeftY + PIXEL_SIZE * 2, 5 * PIXEL_SIZE, 5 * PIXEL_SIZE))
    fill(Rectangle2D.Float(topLeftX + 2 * PIXEL_SIZE, topLeftY + PIXEL_SIZE, 4 * PIXEL_SIZE, 7 * PIXEL_SIZE))

    color = Color.WHITE
    fill(Rectangle2D.Float(topLeftX + 2 * PIXEL_SIZE, topLeftY, 3 * PIXEL_SIZE, 6 * PIXEL_SIZE))
    fill(Rectangle2D.Float(topLeftX + PIXEL_SIZE, topLeftY + PIXEL_SIZE, 5 * PIXEL_SIZE, 5 * PIXEL_SIZE))
    fill(Rectangle2D.Float(topLeftX + PIXEL_SIZE, topLeftY + PIXEL_SIZE, 4 * PIXEL_SIZE, 6 * PIXEL_SIZE))
    fill(Rectangle2D.Float(topLeftX, topLeftY + PIXEL_SIZE * 2, stoneWidth, 3 * PIXEL_SIZE))
    fill(Rectangle2D.Float(topLeftX, topLeftY + PIXEL_SIZE * 2, 5 * PIXEL_SIZE, 4 * PIXEL_SIZE))
}

fun Graphics2D.drawBlackStone(x: Int, y: Int, width: Int, height: Int, boardWidth: Int, boardHeight: Int) {
    val gx = playAreaX(x, width, boardWidth)
    val gy = playAreaY(y, height, boardHeight)

    val stoneWidth = 7 * PIXEL_SIZE
    val stoneHeight = 8 * PIXEL_SIZE

    val topLeftX = gx - (stoneWidth / 2)
    val topLeftY = gy - (stoneHeight / 2)

    color = Color.BLACK
    fill(Rectangle2D.Float(topLeftX + 2 * PIXEL_SIZE, topLeftY, 3 * PIXEL_SIZE, stoneHeight))
    fill(Rectangle2D.Float(topLeftX + PIXEL_SIZE, topLeftY + PIXEL_SIZE, 5 * PIXEL_SIZE, stoneHeight - 2 * PIXEL_SIZE))
    fill(Rectangle2D.Float(topLeftX, topLeftY + 2 * PIXEL_SIZE, 7 * PIXEL_SIZE, stoneHeight - 4 * PIXEL_SIZE))

    color = Color.WHITE
    fill(Rectangle2D.Float(topLeftX + PIXEL_SIZE * 2, topLeftY + PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE))
    fill(Rectangle2D.Float(topLeftX + PIXEL_SIZE, topLeftY + 2 * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE * 3))
}

private fun playAreaOffsetX(width: Int): Float {
    val playAreaWidth = playAreaWidth(width)
    return playAreaWidth * PLAY_AREA_OFFSET_FACTOR
}

private fun playAreaOffsetY(height: Int): Float {
    val playAreaHeight = playAreaHeight(height)
    return playAreaHeight * PLAY_AREA_OFFSET_FACTOR
}

private fun intersectionWidth(width: Int, boardWidth: Int): Float {
    val playAreaWidth = playAreaWidth(width)
    val offset = playAreaOffsetX(width)
    val actualWidth = playAreaWidth - 2 * offset
    return actualWidth / (boardWidth - 1).toFloat()
}

private fun intersectionHeight(height: Int, boardHeight: Int): Float {
    val playAreaHeight = playAreaHeight(height)
    val offset = playAreaOffsetX(height)
    val actualHeight = playAreaHeight - 2 * offset
    return actualHeight / (boardHeight - 1).toFloat()
}

private fun playAreaX(x: Int, width: Int, boardWidth: Int): Float {
    return playAreaStartX(width) + intersectionWidth(width, boardWidth) * (x - 1)
}

private fun playAreaY(y: Int, height: Int, boardHeight: Int): Float {
    return playAreaStartY(height) + intersectionHeight(height, boardHeight) * (y - 1)
}

private fun Graphics2D.drawIntersections(width: Int, height: Int, boardWidth: Int, boardHeight: Int) {
    stroke = BasicStroke(PIXEL_SIZE)
    color = COLOR_SHADOW

    val intersectionWidth = intersectionWidth(width, boardWidth)
    val intersectionHeight = intersectionHeight(height, boardHeight)

    repeat(boardWidth) { x ->
        val gx = playAreaStartX(width) + intersectionWidth * x
        val startY = playAreaStartY(height)
        val endY = playAreaY(boardHeight, height, boardHeight)
        val line = Line2D.Float(
            gx,
            startY,
            gx,
            endY
        )

        draw(line)
    }

    repeat(boardHeight) { y ->
        val gy = playAreaStartY(height) + intersectionHeight * y
        val startX = playAreaStartX(width)
        val endX = playAreaX(boardWidth, width, boardWidth)
        val line = Line2D.Float(
            startX,
            gy,
            endX,
            gy
        )

        draw(line)
    }
}

private fun Graphics2D.drawBoardThickness(width: Int, height: Int) {
    val startX = gobanStartX(width)
    val startY = boardThicknessStartY(height)

    val shadowWidth = (gobanWidth(width) / PIXEL_SIZE).toInt()
    val shadowHeight = (gobanThickness(height) / PIXEL_SIZE).toInt()

    color = COLOR_SHADOW
    val shadow = Rectangle2D.Float(startX, startY, shadowWidth * PIXEL_SIZE, shadowHeight * PIXEL_SIZE)
    fill(shadow)

    repeat(shadowHeight) { y ->
        val gy = startY + PIXEL_SIZE * y
        val x = if (y % 2 == 0) {
            startX + PIXEL_SIZE
        } else {
            startX
        }
        drawShadowedLine(x, gy, shadowWidth)
    }
}

private fun Graphics2D.drawPillars(width: Int, height: Int) {
    val pillarWidthInPixels = 15
    val gobanWidthInPixels = (gobanWidth(width) / PIXEL_SIZE).toInt()

    val middleXInPixels = gobanWidthInPixels / 2

    val leftPillarMiddleXInPixels = middleXInPixels - gobanWidthInPixels / 3
    val leftPillarStartXInPixels = leftPillarMiddleXInPixels - (pillarWidthInPixels / 2) + 1

    drawPillar(gobanStartX(width) + leftPillarStartXInPixels * PIXEL_SIZE, height)

    val rightPillarMiddleXInPixels = gobanWidthInPixels - leftPillarMiddleXInPixels
    val rightPillarStartXInPixels = rightPillarMiddleXInPixels - (pillarWidthInPixels / 2) + 3

    drawPillar(rightPillarStartXInPixels * PIXEL_SIZE, height)
}

private fun Graphics2D.drawPillar(startX: Float, height: Int) {
    val startY = boardThicknessStartY(height) + (gobanThickness(height) / PIXEL_SIZE).toInt() * PIXEL_SIZE
    drawShadowedLine(startX + 2 * PIXEL_SIZE, startY, 11)
    drawShadowedLine(startX, startY + PIXEL_SIZE * 2, 15)
    drawShadowedLine(startX, startY + PIXEL_SIZE * 4, 15)
    drawShadowedLine(startX + 2 * PIXEL_SIZE, startY + PIXEL_SIZE * 6, 11)
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
