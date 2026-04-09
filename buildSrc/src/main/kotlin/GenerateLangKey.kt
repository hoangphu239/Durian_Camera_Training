import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

fun generateLangKey(input: File, output: File) {

    val json = input.readText()

    val type = object : TypeToken<Map<String, String>>() {}.type

    val map: Map<String, String> = try {
        Gson().fromJson(json, type) ?: emptyMap()
    } catch (e: Exception) {
        println("JSON parse failed: ${e.message}")
        emptyMap()
    }

    val grouped = map.keys
        .mapNotNull { fullKey ->

            val parts = fullKey.split(".")

            if (parts.size < 2) {
                println("Invalid key format: $fullKey")
                return@mapNotNull null
            }

            // ✅ LẤY 2 PHẦN CUỐI
            val groupType = parts[parts.size - 2]
            val key = parts.last()

            Triple(groupType, key, fullKey)
        }
        .groupBy { it.first }

    val builder = StringBuilder()

    builder.appendLine("package com.netsservices.dct.i18n")
    builder.appendLine()
    builder.appendLine("object LangKey {")

    grouped.forEach { (type, items) ->

        val typeName = type.split("_")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

        builder.appendLine("    object $typeName {")

        items.distinctBy { it.second }.forEach { (_, key, fullKey) ->

            val constName = key.split("_")
                .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

            builder.appendLine("        const val $constName = \"$fullKey\"")
        }

        builder.appendLine("    }")
    }

    builder.appendLine("}")

    output.parentFile.mkdirs()
    output.writeText(builder.toString())

    println("LangKey generated: ${output.path}")
}