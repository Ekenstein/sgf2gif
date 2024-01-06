package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import java.awt.Graphics2D

data class Stone(val point: SgfPoint, val color: SgfColor)

interface BoardTheme {
    fun drawEmptyBoard(g: Graphics2D)
    fun drawStone(g: Graphics2D, stone: Stone)
    fun clearPoint(g: Graphics2D, x: Int, y: Int)
}
