package com.netsservices.dct.presentation.common

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


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

fun String.toMacAddress(): String {
    return this
        .take(12)              // chỉ lấy 12 ký tự đầu
        .uppercase()
        .chunked(2)
        .joinToString(":")
}

