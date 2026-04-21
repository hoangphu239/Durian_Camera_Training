package com.netsservices.dct.presentation.helper.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import java.io.ByteArrayOutputStream
import androidx.core.graphics.get
import com.netsservices.dct.presentation.common.Constants

class FrameProcessor {

    data class LaserPoint(val x: Float, val y: Float)
    private data class CandidatePoint(val x: Int, val y: Int, val score: Float)

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

    fun detectLaserCentersWithROI(
        bitmap: Bitmap,
        targets: List<Offset>,
        radius: Float
    ): List<LaserPoint> {

        if (targets.size != 3) return emptyList()

        val result = mutableListOf<LaserPoint>()

        for (target in targets) {
            val p = findBestPointInCircle(bitmap, target, radius) ?: return emptyList()
            result.add(p)
        }

        return if (isTriangleValid(result)) result else emptyList()
    }


    private fun findBestPointInCircle(
        bitmap: Bitmap,
        center: Offset,
        radius: Float
    ): LaserPoint? {

        val r = radius.toInt()
        val cx = center.x.toInt()
        val cy = center.y.toInt()

        val width = bitmap.width
        val height = bitmap.height

        var bestScore = 0f
        var bestPoint: LaserPoint? = null

        for (dy in -r..r) {
            val y = cy + dy
            if (y !in 0..<height) continue

            for (dx in -r..r) {
                val x = cx + dx
                if (x !in 0..<width) continue

                if (dx * dx + dy * dy > r * r) continue

                val pixel = bitmap[x, y]

                if (!isBurnColor(pixel)) continue

                val density = computeDarkDensity(bitmap, x, y)

                if (density < Constants.MIN_DARK_DENSITY) continue

                val luma = luminance(pixel)
                val score = computeDarkSpotScore(bitmap, x, y, luma) + density * 50f

                if (score > bestScore) {
                    bestScore = score
                    bestPoint = LaserPoint(x.toFloat(), y.toFloat())
                }
            }
        }

        return if (bestScore > 40f) bestPoint else null
    }

    private fun isBurnColor(pixel: Int): Boolean {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)

        val luma = 0.299f * r + 0.587f * g + 0.114f * b

        if (luma > Constants.MAX_LUMA) return false

        val brownish = (r >= g) && (g >= b)

        val notPureBlack = (r + g + b) > Constants.MIN_RGB_SUM

        return brownish && notPureBlack
    }

    private fun computeDarkDensity(
        bitmap: Bitmap,
        cx: Int,
        cy: Int
    ): Float {

        val radius = Constants.DENSITY_RADIUS
        val width = bitmap.width
        val height = bitmap.height

        var darkCount = 0
        var total = 0

        for (dy in -radius..radius) {
            val y = cy + dy
            if (y !in 0..<height) continue

            for (dx in -radius..radius) {
                val x = cx + dx
                if (x !in 0..<width) continue

                if (dx * dx + dy * dy > radius * radius) continue

                val pixel = bitmap[x, y]

                if (isBurnColor(pixel)) {
                    darkCount++
                }

                total++
            }
        }

        return if (total > 0) darkCount.toFloat() / total else 0f
    }

    private fun isTriangleValid(points: List<LaserPoint>): Boolean {
        if (points.size != 3) return false

        val d1 = distance(points[0], points[1])
        val d2 = distance(points[1], points[2])
        val d3 = distance(points[2], points[0])

        val max = maxOf(d1, d2, d3)
        val min = minOf(d1, d2, d3)

        return max / min < 1.5f
    }

    private fun distance(a: LaserPoint, b: LaserPoint): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    private fun luminance(pixel: Int): Float {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return 0.299f * r + 0.587f * g + 0.114f * b
    }

    private fun computeDarkSpotScore(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        centerLuma: Float
    ): Float {
        val near = averageLuminanceInRing(bitmap, x, y, 2, 4)
        val far = averageLuminanceInRing(bitmap, x, y, 5, 8)
        val contrast = ((near + far) * 0.5f) - centerLuma
        val darkness = 255f - centerLuma

        return (contrast * 1.6f) + (darkness * 0.35f)
    }

    private fun averageLuminanceInRing(
        bitmap: Bitmap,
        centerX: Int,
        centerY: Int,
        innerRadius: Int,
        outerRadius: Int
    ): Float {
        val width = bitmap.width
        val height = bitmap.height
        val innerSq = innerRadius * innerRadius
        val outerSq = outerRadius * outerRadius

        var sum = 0f
        var count = 0

        for (dy in -outerRadius..outerRadius) {
            val y = centerY + dy
            if (y !in 0..<height) continue

            for (dx in -outerRadius..outerRadius) {
                val x = centerX + dx
                if (x !in 0..<width) continue

                val distSq = dx * dx + dy * dy
                if (distSq !in innerSq..outerSq) continue

                val pixel = bitmap[x, y]
                val luma = luminance(pixel)
                sum += luma
                count++
            }
        }
        return if (count > 0) sum / count else 0f
    }
}
