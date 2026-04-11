import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

fun generateLangKey(input: File, output: File) {

    println("==== GENERATE LANG KEY ====")
    println("Input path: ${input.absolutePath}")
    println("File exists: ${input.exists()}")

    if (!input.exists()) {
        throw RuntimeException("File not found: ${input.absolutePath}")
    }

    val gson = Gson()
    val raw = input.readText()

    println("==== RAW JSON ====")
    println(raw.take(200))

    val cleanedRaw = raw
        .trim()
        .replace("\uFEFF", "")     // remove BOM
        .replace("\u0000", "")     // remove null char
        .replace(Regex("^[^\\{]+"), "") // remove mọi thứ trước dấu {

    println("==== CLEANED JSON ====")
    println(cleanedRaw.take(200))

    val json = try {
        if (cleanedRaw.startsWith("\"")) {
            println("JSON bị stringify → unwrap")
            val unwrapped = gson.fromJson(cleanedRaw, String::class.java)

            println("==== UNWRAPPED JSON ====")
            println(unwrapped.take(200))

            unwrapped
        } else {
            cleanedRaw
        }
    } catch (e: Exception) {
        throw RuntimeException("Failed to unwrap JSON: ${e.message}")
    }

    println("==== FINAL JSON USED ====")
    println(json.take(200))

    val type = object : TypeToken<Map<String, String>>() {}.type

    val map: Map<String, String> = try {
        gson.fromJson(json, type)
    } catch (e: Exception) {
        println("JSON ERROR CONTENT:")
        println(json.take(300))
        throw RuntimeException("JSON parse failed: ${e.message}")
    }

    println("Map size: ${map.size}")

    if (map.isEmpty()) {
        throw RuntimeException("Map is empty → JSON sai format")
    }

    val grouped = map.keys
        .mapNotNull { fullKey ->

            val parts = fullKey.split('.')

            if (parts.size < 2) {
                println("Invalid key format: $fullKey")
                return@mapNotNull null
            }

            val groupType = parts[parts.size - 2]
            val key = parts.last()

            Triple(groupType, key, fullKey)
        }
        .groupBy { it.first }

    println("Grouped size: ${grouped.size}")

    val builder = StringBuilder()

    builder.appendLine("package com.netsservices.dct.i18n")
    builder.appendLine()
    builder.appendLine("object LangKey {")

    grouped.forEach { (typeNameRaw, items) ->

        val typeName = typeNameRaw.split("_")
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

    println("==== GENERATED CODE ====")
    println(builder.toString())

    output.parentFile.mkdirs()
    output.writeText(builder.toString())

    println("LangKey generated at: ${output.absolutePath}")
}