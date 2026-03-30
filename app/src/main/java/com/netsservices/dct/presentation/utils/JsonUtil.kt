package com.netsservices.dct.presentation.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonUtil {

    val gson = Gson()

    fun <T> toJson(data: T): String {
        return gson.toJson(data)
    }

    inline fun <reified T> fromJson(json: String): T {
        return gson.fromJson(json, T::class.java)
    }

    inline fun <reified T> fromJsonList(json: String): List<T> {
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(json, type)
    }
}