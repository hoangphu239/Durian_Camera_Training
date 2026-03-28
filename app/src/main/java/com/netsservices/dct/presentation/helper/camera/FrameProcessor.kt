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

    private var lastSentTime = 0L
    private val frameIntervalMs = 100L // ~10 FPS

    fun shouldProcess(): Boolean {
        val now = System.currentTimeMillis()
        return if (now - lastSentTime >= frameIntervalMs) {
            lastSentTime = now
            true
        } else false
    }

    fun process(imageProxy: ImageProxy): ByteArray? {
        return try {
            val nv21 = yuv420888ToNv21(imageProxy)

            val jpeg = nv21ToJpeg(
                nv21,
                imageProxy.width,
                imageProxy.height
            )

            rotateJpeg(jpeg, imageProxy.imageInfo.rotationDegrees)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🔥 YUV → NV21
    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val ySize = yBuffer.remaining()
        val nv21 = ByteArray(width * height * 3 / 2)

        yBuffer.get(nv21, 0, ySize)

        val pixelStride = uPlane.pixelStride
        val rowStride = uPlane.rowStride

        val uArray = ByteArray(uBuffer.remaining())
        val vArray = ByteArray(vBuffer.remaining())
        uBuffer.get(uArray)
        vBuffer.get(vArray)

        var offset = ySize
        val uvWidth = width / 2
        val uvHeight = height / 2

        for (row in 0 until uvHeight) {
            for (col in 0 until uvWidth) {
                val index = row * rowStride + col * pixelStride
                nv21[offset++] = vArray[index]
                nv21[offset++] = uArray[index]
            }
        }

        return nv21
    }

    // NV21 → JPEG
    private fun nv21ToJpeg(nv21: ByteArray, width: Int, height: Int): ByteArray {
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, out)
        return out.toByteArray()
    }

    // FIX ROTATION
    private fun rotateJpeg(jpeg: ByteArray, rotationDegrees: Int): ByteArray {
        if (rotationDegrees == 0) return jpeg

        val bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
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
        rotated.compress(Bitmap.CompressFormat.JPEG, 80, out)

        return out.toByteArray()
    }
}