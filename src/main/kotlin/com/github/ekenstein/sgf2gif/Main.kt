package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfCollection
import com.github.ekenstein.sgf.SgfException
import com.github.ekenstein.sgf.SgfGameTree
import com.github.ekenstein.sgf.editor.SgfEditor
import com.github.ekenstein.sgf.editor.goToNextMove
import com.github.ekenstein.sgf.editor.repeat
import com.github.ekenstein.sgf.parser.from
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

private class PathArgType : ArgType<Path>(false) {
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

private class SgfArgType : ArgType<SgfGameTree>(false) {
    override val description: kotlin.String
        get() = ""

    override fun convert(value: kotlin.String, name: kotlin.String): SgfGameTree {
        val path = try {
            Path.of(value)
        } catch (ex: InvalidPathException) {
            throw ParsingException("Option $name is expected to be a path. $value is provided.")
        }

        if (!path.exists()) {
            throw ParsingException("Option $name is expected to exist.")
        }

        return try {
            SgfCollection.from(path).trees.head
        } catch (ex: SgfException.ParseError) {
            throw ParsingException("Option $name is expected to be a valid SGF file.")
        }
    }
}

private val parser = ArgParser("sgf2gif")
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

private val loop by parser.option(
    type = ArgType.Boolean,
    fullName = "loop",
    shortName = "l",
    description = "Whether the animation should be looped or not"
).default(true)

private val width by parser.option(
    type = ArgType.Int,
    fullName = "width",
    description = "The width of the image."
).default(1000)

private val height by parser.option(
    type = ArgType.Int,
    fullName = "height",
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
    description = "Whether captured stones should be removed or kept in the animation."
).default(false)

fun main(args: Array<String>) {
    parser.parse(args)
    val editor = SgfEditor(sgf).repeat(moveNumber) {
        it.goToNextMove()
    }

    val outputFile = output.toFile()
    FileImageOutputStream(outputFile).use {
        convertPositionToAnimatedGif(
            outputStream = it,
            editor = editor,
            width = width,
            height = height,
            delay = delay.seconds,
            loop = loop,
            showMoveNumber = showMoveNumber,
            removeCapturedStones = removeCapturedStones
        )
    }
}



