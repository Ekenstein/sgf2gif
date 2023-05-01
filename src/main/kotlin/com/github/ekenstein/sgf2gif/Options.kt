package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfCollection
import com.github.ekenstein.sgf.SgfException
import com.github.ekenstein.sgf.SgfGameTree
import com.github.ekenstein.sgf.parser.from
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException
import kotlinx.cli.default
import kotlinx.cli.required
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.exists

class Options(parser: ArgParser) {
    val sgf by parser.option(
        type = SgfArgType(),
        description = "The SGF-file to convert to a GIF",
        shortName = "f",
        fullName = "file"
    ).required()

    val output by parser.option(
        type = PathArgType(),
        description = "The destination file to write the GIF to. (Optional)",
        shortName = "o",
        fullName = "output"
    )

    val theme by parser.option(
        type = ArgType.Choice<Theme>(),
        fullName = "theme",
        description = "The theme to render the board with"
    ).default(Theme.NES)

    val loop by parser.option(
        type = ArgType.Boolean,
        fullName = "loop",
        shortName = "l",
        description = "Whether the animation should be looped or not"
    ).default(false)

    val width by parser.option(
        type = ArgType.Int,
        fullName = "width",
        shortName = "w",
        description = "The width of the image."
    ).default(1000)

    val height by parser.option(
        type = ArgType.Int,
        fullName = "height",
        shortName = "h",
        description = "The height of the image."
    ).default(1000)

    val moveNumber by parser.option(
        type = ArgType.Int,
        fullName = "move-number",
        shortName = "mn",
        description = "The move number up to which the animation will run to."
    ).default(Int.MAX_VALUE)

    val delay by parser.option(
        type = ArgType.Int,
        fullName = "delay",
        shortName = "d",
        description = "The delay between frames in seconds."
    ).default(2)
}

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

private class SgfArgType : ArgType<Pair<File, SgfGameTree>>(true) {
    override val description: kotlin.String
        get() = ""

    override fun convert(value: kotlin.String, name: kotlin.String): Pair<File, SgfGameTree> {
        val file = File(value)

        if (!file.exists()) {
            throw ParsingException("Option $name is expected to exist. The path was $value")
        }

        return try {
            val collection = file.inputStream().use { inputStream ->
                SgfCollection.from(inputStream) {
                    ignoreMalformedProperties = true
                }
            }

            val tree = collection.trees.head
            file to tree
        } catch (ex: SgfException.ParseError) {
            throw ParsingException("Option $name is expected to be a valid SGF file.")
        }
    }
}
