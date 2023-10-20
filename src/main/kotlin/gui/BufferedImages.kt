package gui

import java.awt.image.BufferedImage

fun BufferedImage.scaled(scale: Int): BufferedImage {
    require(scale >= 2) { "scale $scale too small" }

    val srcWidth = width
    val srcHeight = height
    val src = IntArray(srcWidth * srcHeight)
    getRGB(0, 0, srcWidth, srcHeight, src, 0, srcWidth)

    val dstWidth = srcWidth * scale
    val dstHeight = srcHeight * scale
    val dst = IntArray(dstWidth * dstHeight)

    var j = 0
    for (y in 0 until dstHeight) {
        val i = (y / scale) * srcHeight
        for (x in 0 until dstWidth) {
            dst[j++] = src[i + (x / scale)]
        }
    }

    val scaled = BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_INT_ARGB)
    scaled.setRGB(0, 0, dstWidth, dstHeight, dst, 0, dstWidth)
    return scaled
}

fun BufferedImage.rotatedCounterclockwise(): BufferedImage {

    val srcWidth = width
    val srcHeight = height
    val src = IntArray(srcWidth * srcHeight)
    val dst = IntArray(srcHeight * srcWidth)
    getRGB(0, 0, srcWidth, srcHeight, src, 0, srcWidth)

    var j = 0
    for (x in srcWidth - 1 downTo 0) {
        var i = x
        for (y in 0 until srcHeight) {
            dst[j++] = src[i]
            i += srcWidth
        }
    }

    val rotated = BufferedImage(srcHeight, srcWidth, BufferedImage.TYPE_INT_ARGB)
    rotated.setRGB(0, 0, srcHeight, srcWidth, dst, 0, srcHeight)
    return rotated
}
