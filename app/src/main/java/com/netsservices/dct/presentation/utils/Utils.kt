package com.netsservices.dct.presentation.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import com.netsservices.dct.BuildConfig
import java.util.Locale

object Utils {

    @SuppressLint("HardwareIds")
    fun getDeviceID(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) + ""

    @SuppressLint("NewApi")
    fun generateCapturedAt(): String {
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(java.time.ZoneOffset.UTC)
        return formatter.format(java.time.Instant.now())
    }

    fun getAppVersion(): String = BuildConfig.VERSION_NAME

    fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun getCountryInfo(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Pair<String?, String?> {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        val countryCode = address?.countryCode
        val countryName = address?.countryName
        return Pair(countryCode, countryName)
    }

    fun saveJpegToGallery(
        context: Context,
        bytes: ByteArray
    ) {
        val resolver = context.contentResolver
        val name = "camera_${System.currentTimeMillis()}.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return

        try {
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(bytes)
                stream.flush()
            } ?: throw Exception("OutputStream is null")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(uri, null, null)
        }
    }

    fun getImageSize(bytes: ByteArray): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return Pair(options.outWidth, options.outHeight)
    }
}