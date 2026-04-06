package com.netsservices.dct.presentation.helper.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

class FrameProcessor {

    fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = ByteArrayOutputStream()

        yuvImage.compressToJpeg(
            Rect(0, 0, image.width, image.height),
            100,
            out
        )

        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun bitmapToJpeg(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            95,
            stream
        )

        return stream.toByteArray()
    }

    fun cropAndResize(src: Bitmap): Bitmap {
        val targetRatio = 1920f / 1080f
        val srcRatio = src.width.toFloat() / src.height.toFloat()

        val newWidth: Int
        val newHeight: Int
        val xOffset: Int
        val yOffset: Int

        if (srcRatio > targetRatio) {
            newHeight = src.height
            newWidth = (targetRatio * newHeight).toInt()
            xOffset = (src.width - newWidth) / 2
            yOffset = 0
        } else {
            newWidth = src.width
            newHeight = (newWidth / targetRatio).toInt()
            xOffset = 0
            yOffset = (src.height - newHeight) / 2
        }

        val cropped = Bitmap.createBitmap(src, xOffset, yOffset, newWidth, newHeight)
        return cropped.scale(1920, 1080)
    }
}