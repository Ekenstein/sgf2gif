package com.github.ekenstein.sgf2gif

import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadataNode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

private const val ATTRIBUTE_DELAY_TIME = "delayTime"
private const val ATTRIBUTE_TRANSPARENT_COLOR_INDEX = "transparentColorIndex"
private const val ATTRIBUTE_TRANSPARENT_COLOR_FLAG = "transparentColorFlag"
private const val ATTRIBUTE_USER_INPUT_FLAG = "userInputFlag"
private const val ATTRIBUTE_DISPOSAL_METHOD = "disposalMethod"
private const val ATTRIBUTE_APPLICATION_ID = "applicationID"
private const val ATTRIBUTE_AUTHENTICATION_CODE = "authenticationCode"
private const val NODE_APPLICATION_EXTENSIONS = "ApplicationExtensions"
private const val NODE_GRAPHIC_CONTROL_EXTENSION = "GraphicControlExtension"
private const val NODE_APPLICATION_EXTENSION = "ApplicationExtension"

class GifMetadata(private val rootNode: IIOMetadataNode) {
    val applicationExtensions: ApplicationExtensions
        get() = ApplicationExtensions(findOrAddNode(NODE_APPLICATION_EXTENSIONS))

    val graphicControlExtension: GraphicControlExtension
        get() = GraphicControlExtension(findOrAddNode(NODE_GRAPHIC_CONTROL_EXTENSION))

    fun setLooping() {
        applicationExtensions.addApplicationExtension {
            applicationId = "NETSCAPE"
            authenticationCode = "2.0"
            userObject = byteArrayOf(
                0x1,
                (0 and 0xFF).toByte(),
                (0 shr 8 and 0xFF).toByte()
            )
        }
    }

    private fun findOrAddNode(name: String) = getNodes().firstOrNull { it.nodeName.equals(name, true) }
        ?: addNode(name)

    private fun addNode(name: String): IIOMetadataNode {
        val node = IIOMetadataNode(name)
        rootNode.appendChild(node)
        return node
    }

    private fun getNodes() = sequence {
        for (i in 0 until rootNode.length) {
            val item = rootNode.item(i) as IIOMetadataNode
            yield(item)
        }
    }

    companion object {
        fun fromInputStream(inputStream: InputStream): GifMetadata {
            val imageReader = ImageIO.getImageReadersBySuffix("gif").next()
                ?: error("Failed to get an image reader for GIF")

            imageReader.input = ImageIO.createImageInputStream(inputStream)

            val imageMetadata = imageReader.getImageMetadata(0)
            val node = imageMetadata.getAsTree(imageMetadata.nativeMetadataFormatName) as IIOMetadataNode
            return GifMetadata(node)
        }
    }
}

class ApplicationExtensions(private val node: IIOMetadataNode) {
    fun addApplicationExtension(block: ApplicationExtension.() -> Unit) {
        val child = IIOMetadataNode(NODE_APPLICATION_EXTENSION)
        ApplicationExtension(child).apply(block)
        node.appendChild(child)
    }

    fun getApplicationExtensions() = node.children.map(::ApplicationExtension)
}

class ApplicationExtension(private val node: IIOMetadataNode) {
    var applicationId: String
        get() = node.getAttribute(ATTRIBUTE_APPLICATION_ID)
        set(value) {
            node.setAttribute(ATTRIBUTE_APPLICATION_ID, value)
        }

    var authenticationCode: String
        get() = node.getAttribute(ATTRIBUTE_AUTHENTICATION_CODE)
        set(value) {
            node.setAttribute(ATTRIBUTE_AUTHENTICATION_CODE, value)
        }

    var userObject: ByteArray?
        get() = node.userObject as? ByteArray
        set(value) {
            node.userObject = value
        }
}

class GraphicControlExtension(private val node: IIOMetadataNode) {
    /**
     * The time to delay between frames
     */
    var delayTime: Duration
        get() {
            val stringValue = node.getAttribute(ATTRIBUTE_DELAY_TIME)
            val longValue = stringValue.toLongOrNull()
                ?: 0

            val milliseconds = longValue * 10
            return milliseconds.milliseconds
        }
        set(value) {
            val valueInMs = value.toLong(DurationUnit.MILLISECONDS) / 10
            node.setAttribute(ATTRIBUTE_DELAY_TIME, valueInMs.toString())
        }

    /**
     * True if the frame should be advanced based on user input
     */
    var userInputFlag: Boolean
        get() = node.getAttribute(ATTRIBUTE_USER_INPUT_FLAG).toBooleanStrictOrNull()
            ?: false
        set(value) {
            node.setAttribute(ATTRIBUTE_USER_INPUT_FLAG, value.toString().uppercase())
        }

    /**
     * True if a transparent color exists
     */
    var transparentColorFlag: Boolean
        get() = node.getAttribute(ATTRIBUTE_TRANSPARENT_COLOR_FLAG).toBooleanStrictOrNull()
            ?: false
        set(value) {
            node.setAttribute(ATTRIBUTE_TRANSPARENT_COLOR_FLAG, value.toString().uppercase())
        }

    /**
     * The transparent color, if transparentColorFlag is true.
     * Min value: 0 (inclusive)
     * Max value: 255 (inclusive)
     */
    var transparentColorIndex: Int
        get() = node.getAttribute(ATTRIBUTE_TRANSPARENT_COLOR_INDEX).toIntOrNull()
            ?: 0
        set(value) {
            node.setAttribute(ATTRIBUTE_TRANSPARENT_COLOR_INDEX, value.toString())
        }

    /**
     * The disposal method for this frame
     */
    var disposalMethod: DisposalMethod
        get() {
            val allDisposalMethods = DisposalMethod.entries.associateBy { it.asString }
            val value = node.getAttribute(ATTRIBUTE_DISPOSAL_METHOD)
            return allDisposalMethods[value]
                ?: DisposalMethod.None
        }
        set(value) {
            node.setAttribute(ATTRIBUTE_DISPOSAL_METHOD, value.asString)
        }
}

enum class DisposalMethod {
    None,
    DoNotDispose,
    RestoreToBackgroundColor,
    RestoreToPrevious,
    UndefinedDisposalMethod4,
    UndefinedDisposalMethod5,
    UndefinedDisposalMethod6,
    UndefinedDisposalMethod7
}

private val DisposalMethod.asString
    get() = when (this) {
        DisposalMethod.None -> "none"
        DisposalMethod.DoNotDispose -> "doNotDispose"
        DisposalMethod.RestoreToBackgroundColor -> "restoreToBackgroundColor"
        DisposalMethod.RestoreToPrevious -> "restoreToPrevious"
        DisposalMethod.UndefinedDisposalMethod4 -> "undefinedDisposalMethod4"
        DisposalMethod.UndefinedDisposalMethod7 -> "undefinedDisposalMethod7"
        DisposalMethod.UndefinedDisposalMethod5 -> "undefinedDisposalMethod5"
        DisposalMethod.UndefinedDisposalMethod6 -> "undefinedDisposalMethod6"
    }

private val IIOMetadataNode.children get() = sequence {
    for (i in 0 until childNodes.length) {
        val childNode = childNodes.item(i) as IIOMetadataNode
        yield(childNode)
    }
}
