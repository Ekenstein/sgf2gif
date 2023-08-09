package com.github.ekenstein.sgf2gif

import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.ImageOutputStream

fun main(args: Array<String>) {
    val options = Options.parse(args)

    openImageOutputStream(options).use { outputStream ->
        options.theme.render(outputStream, options)
    }
}

private fun openImageOutputStream(options: Options): ImageOutputStream = options.output?.let(::FileImageOutputStream)
    ?: ImageIO.createImageOutputStream(System.out)
