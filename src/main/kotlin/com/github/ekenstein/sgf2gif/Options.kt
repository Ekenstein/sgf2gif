package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.SgfCollection
import com.github.ekenstein.sgf.SgfException
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
import java.io.File
import java.io.InputStream
import java.nio.file.InvalidPathException
import kotlin.time.Duration.Companion.seconds

const val DEFAULT_WIDTH = 1000
const val DEFAULT_HEIGHT = 1000
const val DEFAULT_DELAY_IN_SECONDS = 2.0
const val DEFAULT_SHOW_MARKER = false
const val DEFAULT_LOOP = false

class Options private constructor(parser: ArgParser) {
    private val inputFile by parser.option(
        type = FileArgType(true),
        description = "The SGF-file to convert to a GIF. If not stated, the SGF will be read from the std-in.",
        shortName = "f",
        fullName = "file"
    )

    val output by parser.option(
        type = FileArgType(false),
        description = "The destination file to write the GIF to. If not stated, the output will be written to std-out.",
        shortName = "o",
        fullName = "output"
    )

    private val themeType by parser.option(
        type = ArgType.Choice<Theme>(),
        fullName = "theme",
        description = "The theme to render the board with"
    ).default(Theme.NES)

    val loop by parser.option(
        type = ArgType.Boolean,
        fullName = "loop",
        shortName = "l",
        description = "Whether the animation should be looped or not"
    ).default(DEFAULT_LOOP)

    val showMarker by parser.option(
        type = ArgType.Boolean,
        fullName = "show-marker",
        description = "Whether the last move should be marked or not"
    ).default(DEFAULT_SHOW_MARKER)

    val width by parser.option(
        type = ArgType.Int,
        fullName = "width",
        shortName = "w",
        description = "The width of the image."
    ).default(DEFAULT_WIDTH)

    val height by parser.option(
        type = ArgType.Int,
        fullName = "height",
        shortName = "h",
        description = "The height of the image."
    ).default(DEFAULT_HEIGHT)

    private val moveNumber by parser.option(
        type = ArgType.Int,
        fullName = "move-number",
        shortName = "mn",
        description = "The move number up to which the animation will run to."
    ).default(Int.MAX_VALUE)

    private val delay by parser.option(
        type = ArgType.Double,
        fullName = "delay",
        shortName = "d",
        description = "The delay between frames in seconds."
    ).default(DEFAULT_DELAY_IN_SECONDS)

    val delayBetweenFrames by lazy {
        delay.seconds
    }

    val sgf by lazy {
        val sgf = when (val file = inputFile) {
            null -> readSgf(System.`in`)
            else -> file.inputStream().use(::readSgf)
        }

        SgfEditor(sgf)
            .tryRepeat(moveNumber) { it.goToNextMove() }
            .orElse { it.goToLastNode().stay() }
            .get()
    }

    val theme by lazy {
        val (boardWidth, boardHeight) = sgf.boardSize()
        when (themeType) {
            Theme.NES -> Nes(width, height, boardWidth, boardHeight)
            Theme.Classic -> Classic(width, height, boardWidth, boardHeight)
        }
    }

    companion object {
        fun parse(args: Array<String>): Options {
            val parser = ArgParser("sgf2gif", useDefaultHelpShortName = false)
            val options = Options(parser)
            parser.parse(args)
            return options
        }
    }
}

private class FileArgType(private val requireExist: kotlin.Boolean) : ArgType<File>(true) {
    override val description: kotlin.String
        get() = ""

    override fun convert(value: kotlin.String, name: kotlin.String): File {
        return try {
            val file = File(value)
            if (requireExist && !file.exists()) {
                throw ParsingException("Option $name file does not exist. $value is provided.")
            }
            file
        } catch (ex: InvalidPathException) {
            throw ParsingException("Option $name is expected to be a path. $value is provided.")
        }
    }
}

private fun readSgf(inputStream: InputStream) = try {
    val collection = SgfCollection.from(inputStream) {
        ignoreMalformedProperties = true
    }

    collection.trees.head
} catch (ex: SgfException.ParseError) {
    throw ParsingException("Expected a valid SGF file.")
}
