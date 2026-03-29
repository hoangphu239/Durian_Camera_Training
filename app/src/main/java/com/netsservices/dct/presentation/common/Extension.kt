package com.netsservices.dct.presentation.common

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Locale


fun Context.setAppLocale(language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)

    resources.updateConfiguration(config, resources.displayMetrics)
    return createConfigurationContext(config)
}

fun Context.saveImageToGallery(bitmap: Bitmap) {
    val name = "thermal_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    uri?.let {
        resolver.openOutputStream(it)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
    }
}

fun String.toRequest() =
    this.toRequestBody("text/plain".toMediaType())

fun Int.toRequest() =
    this.toString().toRequestBody("text/plain".toMediaType())

fun Float.toRequest() =
    this.toString().toRequestBody("text/plain".toMediaType())

fun Double.toRequest() =
    this.toString().toRequestBody("text/plain".toMediaType())

fun File.toMultipart(
    partName: String,
    mimeType: String = "image/*"
): MultipartBody.Part {
    val requestBody = asRequestBody(mimeType.toMediaType())
    return MultipartBody.Part.createFormData(partName, name, requestBody)
}

fun ByteArray.toRequestBody(
    mimeType: String = "image/jpeg"
): RequestBody {
    return this.toRequestBody(mimeType.toMediaType())
}

fun ByteArray.toMultipart(
    partName: String = "image",
    mimeType: String = "image/*"
): MultipartBody.Part {
    val fileName = "frame_${System.currentTimeMillis()}.jpeg"
    val requestBody = this.toRequestBody(mimeType)
    return MultipartBody.Part.createFormData(partName, fileName, requestBody)
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

