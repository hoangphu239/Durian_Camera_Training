package com.netsservices.dct.presentation.helper.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import androidx.core.graphics.get

class FrameProcessor {

    data class LaserPoint(val x: Float, val y: Float)

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
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)

        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    }

    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun bitmapToJpeg(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        return stream.toByteArray()
    }

    fun detectLaserCenters(bitmap: Bitmap): List<LaserPoint> {

        val candidates = mutableListOf<Triple<Int, Int, Int>>()

        val step = 2

        for (y in 0 until bitmap.height step step) {
            for (x in 0 until bitmap.width step step) {

                val pixel = bitmap[x, y]

                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val intensity = r + g + b

                // 🔥 chỉ lấy lõi trắng
                if (r > 240 && g > 240 && b > 240) {
                    candidates.add(Triple(x, y, intensity))
                }
            }
        }

        if (candidates.size < 3) return emptyList()

        val brightest = candidates
            .sortedByDescending { it.third }
            .take(120)

        return pickTop3Clusters(brightest)
    }

    private fun pickTop3Clusters(
        points: List<Triple<Int, Int, Int>>
    ): List<LaserPoint> {

        val clusters = mutableListOf<MutableList<Triple<Int, Int, Int>>>()
        val threshold = 60

        for (p in points) {

            var added = false

            for (cluster in clusters) {
                val c = cluster.first()

                val dx = p.first - c.first
                val dy = p.second - c.second

                if (dx * dx + dy * dy < threshold * threshold) {
                    cluster.add(p)
                    added = true
                    break
                }
            }

            if (!added) {
                clusters.add(mutableListOf(p))
            }
        }

        if (clusters.size < 3) return emptyList()

        return clusters
            .sortedByDescending { it.size }
            .take(3)
            .map { cluster ->
                val best = cluster.maxByOrNull { it.third }!!
                LaserPoint(best.first.toFloat(), best.second.toFloat())
            }
    }
}