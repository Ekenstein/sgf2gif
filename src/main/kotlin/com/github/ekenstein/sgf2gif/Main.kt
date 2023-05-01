package com.github.ekenstein.sgf2gif

import com.github.ekenstein.sgf.editor.*
import com.github.ekenstein.sgf.utils.get
import com.github.ekenstein.sgf.utils.orElse
import com.github.ekenstein.sgf2gif.themes.Classic
import com.github.ekenstein.sgf2gif.themes.Nes
import kotlinx.cli.ArgParser
import java.io.File
import java.nio.file.Paths
import java.text.NumberFormat
import javax.imageio.stream.FileImageOutputStream
import kotlin.time.Duration.Companion.seconds

private val parser = ArgParser("sgf2gif", useDefaultHelpShortName = false)

private val percentageFormat = NumberFormat.getPercentInstance()

fun main(args: Array<String>) {
    val options = Options(parser)
    parser.parse(args)

    val (inputFile, sgf) = options.sgf

    val editor = SgfEditor(sgf)
        .tryRepeat(options.moveNumber) { it.goToNextMove() }
        .orElse { it.goToLastNode().stay() }
        .get()

    val (boardWidth, boardHeight) = editor.boardSize()
    val outputFile = options.output?.toFile()
        ?: createOutputFile(inputFile.nameWithoutExtension)

    FileImageOutputStream(outputFile).use { outputStream ->
        val renderer = when (options.theme) {
            Theme.NES -> Nes(options.width, options.height, boardWidth, boardHeight)
            Theme.Classic -> Classic(options.width, options.height, boardWidth, boardHeight)
        }

        renderer.render(outputStream, editor, options.width, options.height, options.delay.seconds, options.loop) {
            print("\r${percentageFormat.format(it)}")
        }
    }

    println("\nExported the SGF to ${outputFile.absolutePath}")
}

private fun createOutputFile(fileName: String): File {
    val currentWorkingDirectory = System.getProperty("user.dir")
    val fileNameWithGifExtension = "$fileName.gif"
    val filePath = Paths.get(currentWorkingDirectory, fileNameWithGifExtension)

    return filePath.toFile()
}
