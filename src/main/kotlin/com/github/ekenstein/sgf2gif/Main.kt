package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfCollection
import com.github.ekenstein.sgf.SgfException
import com.github.ekenstein.sgf.SgfGameTree
import com.github.ekenstein.sgf.editor.SgfEditor
import com.github.ekenstein.sgf.editor.goToLastNode
import com.github.ekenstein.sgf.editor.goToNextMove
import com.github.ekenstein.sgf.editor.stay
import com.github.ekenstein.sgf.editor.tryRepeat
import com.github.ekenstein.sgf.parser.from
import com.github.ekenstein.sgf.utils.get
import com.github.ekenstein.sgf.utils.orElse
import com.github.ekenstein.sgf2gif.themes.Classic
import com.github.ekenstein.sgf2gif.themes.Nes
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException
import kotlinx.cli.default
import kotlinx.cli.required
import java.nio.file.InvalidPathException
import java.nio.file.Path
import javax.imageio.stream.FileImageOutputStream
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.seconds

private class PathArgType : ArgType<Path>(true) {
    override val description: kotlin.String
        get() = ""

    override fun convert(value: kotlin.String, name: kotlin.String): Path {
        return try {
            Path.of(value)
        } catch (ex: InvalidPathException) {
            throw ParsingException("Option $name is expected to be a path. $value is provided.")
        }
    }
}

private class SgfArgType : ArgType<SgfGameTree>(true) {
    override val description: kotlin.String
        get() = ""

    override fun convert(value: kotlin.String, name: kotlin.String): SgfGameTree {
        val path = try {
            Path.of(value)
        } catch (ex: InvalidPathException) {
            throw ParsingException("Option $name is expected to be a path. $value is provided.")
        }

        if (!path.exists()) {
            throw ParsingException("Option $name is expected to exist. The path was $path")
        }

        return try {
            SgfCollection.from(path).trees.head
        } catch (ex: SgfException.ParseError) {
            throw ParsingException("Option $name is expected to be a valid SGF file.")
        }
    }
}

private val parser = ArgParser("sgf2gif", useDefaultHelpShortName = false)
private val sgf by parser.option(
    type = SgfArgType(),
    description = "The SGF-file to convert to a GIF",
    shortName = "f",
    fullName = "file"
).required()

private val output by parser.option(
    type = PathArgType(),
    description = "The destination file to write the GIF to.",
    shortName = "o",
    fullName = "output"
).required()

private val theme by parser.option(
    type = ArgType.Choice<Theme>(),
    fullName = "theme",
    description = "The theme to render the board with"
).default(Theme.NES)

private val loop by parser.option(
    type = ArgType.Boolean,
    fullName = "loop",
    shortName = "l",
    description = "Whether the animation should be looped or not"
).default(true)

private val width by parser.option(
    type = ArgType.Int,
    fullName = "width",
    shortName = "w",
    description = "The width of the image."
).default(1000)

private val height by parser.option(
    type = ArgType.Int,
    fullName = "height",
    shortName = "h",
    description = "The height of the image."
).default(1000)

private val moveNumber by parser.option(
    type = ArgType.Int,
    fullName = "move-number",
    shortName = "mn",
    description = "The move number up to which the animation will run to."
).default(Int.MAX_VALUE)

private val delay by parser.option(
    type = ArgType.Int,
    fullName = "delay",
    shortName = "d",
    description = "The delay between frames in seconds."
).default(2)

private val showMoveNumber by parser.option(
    type = ArgType.Boolean,
    fullName = "show-move-number",
    description = "Whether each stone should be annotated with its move number or not."
).default(true)

private val removeCapturedStones by parser.option(
    type = ArgType.Boolean,
    fullName = "remove-captured-stones",
    shortName = "r",
    description = "Whether captured stones should be removed from the board or not."
).default(false)

fun main(args: Array<String>) {
    parser.parse(args)
    val editor = SgfEditor(sgf)
        .tryRepeat(moveNumber) { it.goToNextMove() }
        .orElse { it.goToLastNode().stay() }
        .get()

    val (boardWidth, boardHeight) = editor.boardSize()
    val outputFile = output.toFile()
    FileImageOutputStream(outputFile).use {
        val renderer = when (theme) {
            Theme.Classic -> Classic(width, height, boardWidth, boardHeight, showMoveNumber)
            Theme.NES -> Nes(width, height, boardWidth, boardHeight)
        }

        renderer.render(it, editor, width, height, delay.seconds, loop, removeCapturedStones)
    }

    println("Exported the SGF to $output")
}
