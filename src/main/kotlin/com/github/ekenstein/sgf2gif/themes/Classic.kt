package com.github.ekenstein.sgf2gif.themes

import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import com.github.ekenstein.sgf2gif.BoardRenderer
import com.github.ekenstein.sgf2gif.Stone
import com.github.ekenstein.sgf2gif.starPoints
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D

private const val BOARD_SCALE = 0.95

class Classic(
    private val width: Int,
    private val height: Int,
    private val boardWidth: Int,
    private val boardHeight: Int,
    private val annotateMoveNumber: Boolean
) : BoardRenderer {
    private val gobanWidth = (BOARD_SCALE * width).toInt()
    private val gobanHeight = (BOARD_SCALE * height).toInt()

    private val xOffset = width - gobanWidth
    private val yOffset = height - gobanHeight

    private val intersectionWidth = gobanWidth / boardWidth
    private val intersectionHeight = gobanHeight / boardHeight

    private fun boardX(x: Int) = intersectionWidth * x + xOffset
    private fun boardY(y: Int) = intersectionHeight * y + yOffset

    override fun drawEmptyBoard(g: Graphics2D) {
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)

        drawIntersections(g)

        if (boardWidth != boardHeight) {
            return
        }

        starPoints(boardWidth).forEach { starPoint ->
            drawStarPoint(g, starPoint)
        }
    }

    override fun drawStone(g: Graphics2D, stone: Stone) {
        val middleX = boardX(stone.point.x - 1)
        val middleY = boardY(stone.point.y - 1)

        val circleWidth = intersectionWidth * 0.90
        val circleHeight = intersectionHeight * 0.90

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
                g.color = Color.BLACK
                g.fill(shape)

                if (stone.moveNumber != null && annotateMoveNumber) {
                    g.color = Color.WHITE
                    g.drawCenteredText(
                        circleWidth.toInt(),
                        circleHeight.toInt(),
                        middleX,
                        middleY,
                        stone.moveNumber.toString()
                    )
                }
            }
            SgfColor.White -> {
                g.color = Color.WHITE
                g.fill(shape)

                g.color = Color.BLACK
                g.stroke = BasicStroke(3F)
                g.draw(shape)

                if (stone.moveNumber != null && annotateMoveNumber) {
                    g.drawCenteredText(
                        circleWidth.toInt(),
                        circleHeight.toInt(),
                        middleX,
                        middleY,
                        stone.moveNumber.toString()
                    )
                }
            }
        }
    }

    private fun drawIntersections(g: Graphics2D) {
        g.color = Color.BLACK

        repeat(boardWidth) { x ->
            g.stroke = if (x == 0 || x == boardWidth - 1) {
                BasicStroke(3F)
            } else {
                BasicStroke(1F)
            }
            val gx = boardX(x)
            g.drawLine(gx, yOffset, gx, yOffset + (intersectionHeight * (boardHeight - 1)))
        }

        repeat(boardHeight) { y ->
            g.stroke = if (y == 0 || y == boardHeight - 1) {
                BasicStroke(3F)
            } else {
                BasicStroke(1F)
            }
            val gy = boardY(y)
            g.drawLine(xOffset, gy, xOffset + (intersectionWidth * (boardWidth - 1)), gy)
        }
    }

    private fun drawStarPoint(
        g: Graphics,
        point: SgfPoint
    ) {
        val middleX = boardX(point.x - 1)
        val middleY = boardY(point.y - 1)

        val circleWidth = (intersectionWidth * 0.20).toInt()
        val circleHeight = (intersectionHeight * 0.20).toInt()

        val topLeftX = middleX - (circleWidth / 2)
        val topLeftY = middleY - (circleHeight / 2)

        g.color = Color.BLACK
        g.fillOval(topLeftX, topLeftY, circleWidth, circleHeight)
    }
}

private fun Graphics.getFontSize(font: Font, text: String): Dimension {
    val metrics = getFontMetrics(font)
    val hgt = metrics.height
    val adv = metrics.stringWidth(text)
    val padding = 10
    return Dimension(adv + padding, hgt + padding)
}

private fun Graphics.findFont(componentSize: Dimension, oldFont: Font, text: String): Font {
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

private fun Graphics.drawCenteredText(width: Int, height: Int, x: Int, y: Int, string: String) {
    font = findFont(Dimension(width, height), font, string)
    val bounds = fontMetrics.getStringBounds(string, this)
    val textHeight = bounds.height
    val textWidth = bounds.width

    val cornerX = x - (textWidth / 2)
    val cornerY = y - (textHeight / 2) + fontMetrics.ascent

    drawString(string, cornerX.toInt(), cornerY.toInt())
}
