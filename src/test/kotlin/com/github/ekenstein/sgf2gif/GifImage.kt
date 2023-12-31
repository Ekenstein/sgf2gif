package com.github.ekenstein.sgf2gif

import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.metadata.IIOMetadataNode

class GifImage private constructor(private val imageReader: ImageReader) {
    private val imageMetaData by lazy {
        val imageMetadata = imageReader.getImageMetadata(0)
        val node = imageMetadata.getAsTree(imageMetadata.nativeMetadataFormatName) as IIOMetadataNode
        GifMetadata(node)
    }

    val numberOfFrames: Int by lazy {
        imageReader.getNumImages(true)
    }

    companion object {
        fun fromStream(inputStream: InputStream): GifImage {
            val imageReader = ImageIO.getImageReadersBySuffix("gif").next()
                ?: error("Failed to get an image reader for GIF")

            imageReader.input = ImageIO.createImageInputStream(inputStream)
            return GifImage(imageReader)
        }

        fun fromByteArray(bytes: ByteArray) = fromStream(ByteArrayInputStream(bytes))
    }
}
