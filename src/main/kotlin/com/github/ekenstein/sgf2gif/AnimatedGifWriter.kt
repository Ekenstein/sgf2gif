package com.github.ekenstein.sgf2gif

import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriter
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageOutputStream
import kotlin.time.Duration

interface GifSequenceWriter {
    fun addFrame(image: RenderedImage)
}

private class GifSequenceWriterImpl(
    private val writer: ImageWriter,
    private val metaData: IIOMetadata
) : GifSequenceWriter {
    override fun addFrame(image: RenderedImage) {
        writer.writeToSequence(
            IIOImage(image, null, metaData),
            writer.defaultWriteParam
        )
    }
}

fun writeGif(outputStream: ImageOutputStream, delay: Duration, loop: Boolean, block: GifSequenceWriter.() -> Unit) {
    val writers = ImageIO.getImageWritersBySuffix("gif")
    require(writers.hasNext()) {
        "There's no GIF image writer for this system"
    }

    val writer = writers.next().apply {
        output = outputStream
        prepareWriteSequence(null)
    }

    val metaData = writer.getDefaultImageMetadata(
        ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB),
        writer.defaultWriteParam
    )

    val root = metaData.getAsTree(metaData.nativeMetadataFormatName) as IIOMetadataNode
    GifMetadata(root).apply {
        graphicControlExtension.apply {
            disposalMethod = DisposalMethod.None
            userInputFlag = false
            transparentColorFlag = false
            delayTime = delay
            transparentColorIndex = 0
        }

        if (loop) {
            setLooping()
        }
    }

    metaData.setFromTree(metaData.nativeMetadataFormatName, root)

    GifSequenceWriterImpl(writer, metaData).block()
    writer.endWriteSequence()
    writer.dispose()
    outputStream.flush()
}
