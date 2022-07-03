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
import kotlin.time.DurationUnit

interface GifSequenceWriter {
    fun addFrame(image: RenderedImage)
}

private class GifSequenceWriterImpl(
    private val writer: ImageWriter,
    private val metaData: IIOMetadata,
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
    root.findOrAddNode("GraphicControlExtension").apply {
        setAttribute("disposalMethod", "none")
        setAttribute("userInputFlag", "FALSE")
        setAttribute("transparentColorFlag", "FALSE")
        setAttribute("delayTime", (delay.toLong(DurationUnit.MILLISECONDS) / 10).toString())
        setAttribute("transparentColorIndex", "0")
    }

    root.findOrAddNode("CommentExtensions").apply {
        setAttribute("CommentExtension", "Created by sgf2gif")
    }

    if (loop) {
        val appExtensionsNode = root.findOrAddNode("ApplicationExtensions")
        val child = IIOMetadataNode("ApplicationExtension").apply {
            setAttribute("applicationID", "NETSCAPE")
            setAttribute("authenticationCode", "2.0")
        }

        child.userObject = byteArrayOf(
            0x1,
            (0 and 0xFF).toByte(),
            (0 shr 8 and 0xFF).toByte()
        )

        appExtensionsNode.appendChild(child)
    }

    metaData.setFromTree(metaData.nativeMetadataFormatName, root)

    GifSequenceWriterImpl(writer, metaData).block()
    writer.endWriteSequence()
    writer.dispose()
}

private fun IIOMetadataNode.findOrAddNode(name: String) = nodes.firstOrNull { it.nodeName.equals(name, true) }
    ?: addNode(name)

private fun IIOMetadataNode.addNode(name: String): IIOMetadataNode {
    val node = IIOMetadataNode(name)
    appendChild(node)
    return node
}

private val IIOMetadataNode.nodes
    get() = sequence {
        for (i in 0 until length) {
            val item = item(i) as IIOMetadataNode
            yield(item)
        }
    }
