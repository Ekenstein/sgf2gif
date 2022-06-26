package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.Move
import com.github.ekenstein.sgf.SgfColor
import com.github.ekenstein.sgf.SgfPoint
import com.github.ekenstein.sgf.SgfProperty
import com.github.ekenstein.sgf.editor.SgfEditor
import com.github.ekenstein.sgf.editor.getMoveNumber
import com.github.ekenstein.sgf.editor.goToPreviousNode
import com.github.ekenstein.sgf.editor.goToRootNode
import com.github.ekenstein.sgf.utils.MoveResult
import com.github.ekenstein.sgf.utils.NonEmptySet
import com.github.ekenstein.sgf.utils.nonEmptySetOf
import kotlin.math.ceil

fun SgfEditor.boardSize() = goToRootNode().currentNode.property<SgfProperty.Root.SZ>()?.let {
    it.width to it.height
} ?: (19 to 19)

fun SgfEditor.getStones(): List<Stone> {
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

fun starPoints(boardSize: Int): Set<SgfPoint> {
    val edgeDistance = edgeDistance(boardSize)
        ?: return emptySet()
    val middle = ceil(boardSize / 2.0).toInt()
    val tengen = SgfPoint(middle, middle)

    fun points(handicap: Int): NonEmptySet<SgfPoint> = when (handicap) {
        2 -> nonEmptySetOf(
            SgfPoint(x = edgeDistance, y = boardSize - edgeDistance + 1),
            SgfPoint(x = boardSize - edgeDistance + 1, y = edgeDistance)
        )
        3 -> nonEmptySetOf(SgfPoint(x = boardSize - edgeDistance + 1, y = boardSize - edgeDistance + 1)) + points(2)
        4 -> nonEmptySetOf(SgfPoint(x = edgeDistance, y = edgeDistance)) + points(3)
        5 -> nonEmptySetOf(tengen) + points(4)
        6 -> nonEmptySetOf(
            SgfPoint(x = edgeDistance, y = middle),
            SgfPoint(x = boardSize - edgeDistance + 1, y = middle)
        ) + points(4)
        7 -> nonEmptySetOf(tengen) + points(6)
        8 -> nonEmptySetOf(
            SgfPoint(middle, edgeDistance),
            SgfPoint(middle, boardSize - edgeDistance + 1)
        ) + points(6)
        9 -> nonEmptySetOf(tengen) + points(8)
        else -> error("Invalid handicap value $handicap")
    }

    return points(starPointsForBoardSize(boardSize))
}

private fun starPointsForBoardSize(boardSize: Int) = when {
    boardSize < 7 -> 0
    boardSize == 7 -> 4
    boardSize % 2 == 0 -> 4
    else -> 9
}

private fun edgeDistance(boardSize: Int) = when {
    boardSize < 7 -> null
    boardSize < 13 -> 3
    else -> 4
}
