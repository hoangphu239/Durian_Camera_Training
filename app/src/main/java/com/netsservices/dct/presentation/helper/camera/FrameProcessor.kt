package com.netsservices.dct.presentation.helper.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

class FrameProcessor {

    fun imageProxyToJpeg(
        image: ImageProxy,
        quality: Int = 100
    ): ByteArray? {
        return try {
            val nv21 = yuv420888ToNv21(image)

            val out = ByteArrayOutputStream()
            val yuvImage = YuvImage(
                nv21,
                ImageFormat.NV21,
                image.width,
                image.height,
                null
            )

            yuvImage.compressToJpeg(
                Rect(0, 0, image.width, image.height),
                quality,
                out
            )

            val jpegBytes = out.toByteArray()

            if (image.imageInfo.rotationDegrees != 0) {
                rotateJpeg(jpegBytes, image.imageInfo.rotationDegrees)
            } else {
                jpegBytes
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun yuv420888ToNv21(image: ImageProxy): ByteArray {

        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val ySize = yBuffer.remaining()
        val nv21 = ByteArray(ySize + width * height / 2)

        // Y
        yBuffer.get(nv21, 0, ySize)

        val chromaRowStride = uPlane.rowStride
        val chromaPixelStride = uPlane.pixelStride

        var offset = ySize

        val uBufferPos = uBuffer.position()
        val vBufferPos = vBuffer.position()

        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {

                val index = row * chromaRowStride + col * chromaPixelStride

                // NV21 = VU
                nv21[offset++] = vBuffer.get(vBufferPos + index)
                nv21[offset++] = uBuffer.get(uBufferPos + index)
            }
        }

        return nv21
    }

    fun rotateJpeg(bytes: ByteArray, rotation: Int): ByteArray {

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val matrix = Matrix().apply {
            postRotate(rotation.toFloat())
        }

        val rotated = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        val out = ByteArrayOutputStream()
        rotated.compress(Bitmap.CompressFormat.JPEG, 100, out)

        bitmap.recycle()
        rotated.recycle()

        return out.toByteArray()
    }
}