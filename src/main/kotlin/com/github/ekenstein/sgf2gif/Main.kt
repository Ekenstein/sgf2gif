package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.editor.SgfEditor
import com.github.ekenstein.sgf.editor.goToLastNode
import com.github.ekenstein.sgf.editor.goToNextMove
import com.github.ekenstein.sgf.editor.stay
import com.github.ekenstein.sgf.editor.tryRepeat
import com.github.ekenstein.sgf.utils.get
import com.github.ekenstein.sgf.utils.orElse
import com.github.ekenstein.sgf2gif.themes.Nes
import kotlinx.cli.ArgParser
import java.text.NumberFormat
import javax.imageio.stream.FileImageOutputStream
import kotlin.time.Duration.Companion.seconds

private val parser = ArgParser("sgf2gif", useDefaultHelpShortName = false)

private val percentageFormat = NumberFormat.getPercentInstance()

fun main(args: Array<String>) {
    val options = Options(parser)
    parser.parse(args)

    val editor = SgfEditor(options.sgf)
        .tryRepeat(options.moveNumber) { it.goToNextMove() }
        .orElse { it.goToLastNode().stay() }
        .get()

    val (boardWidth, boardHeight) = editor.boardSize()
    val outputFile = options.output.toFile()
    FileImageOutputStream(outputFile).use { outputStream ->
        val renderer = when (options.theme) {
            Theme.NES -> Nes(options.width, options.height, boardWidth, boardHeight)
        }

        renderer.render(outputStream, editor, options.width, options.height, options.delay.seconds, options.loop) {
            print("\r${percentageFormat.format(it)}")
        }
    }

    println("\nExported the SGF to ${options.output}")
}
