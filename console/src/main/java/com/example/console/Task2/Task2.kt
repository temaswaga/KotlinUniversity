package com.example.console.Task2

import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest
import kotlin.system.measureTimeMillis

suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            ensureActive()
            digest.update(buffer, 0, bytesRead)
        }
    }
    digest.digest().joinToString("") { "%02x".format(it) }
}

suspend fun findDuplicates(targetDir: File, timeoutSeconds: Long) = coroutineScope {
    println("Сканирование каталога: ${targetDir.absolutePath}")

    val jsonFiles = targetDir.walkTopDown()
        .filter { it.isFile && it.extension.equals("json", ignoreCase = true) }
        .toList()

    println("Найдено JSON-файлов: ${jsonFiles.size}")
    if (jsonFiles.isEmpty()) {
        println("Файлы для анализа не найдены.")
        return@coroutineScope
    }

    val results = withTimeoutOrNull(timeoutSeconds * 1000L) {
        val deferredList = jsonFiles.map { file ->
            async(Dispatchers.IO) {
                delay(100)
                val hash = calculateSha256(file)
                hash to file.name
            }
        }
        deferredList.awaitAll()
    }


    if (results == null) {
        println("Поиск прерван по таймауту")
    } else {
        println("\n--- Результаты анализа ---")
        val duplicates = results.groupBy({ it.first }, { it.second })
            .filter { it.value.size > 1 }

        if (duplicates.isEmpty()) {
            println("Дубликаты не обнаружены. Все файлы уникальны.")
        } else {
            println("Найдены дубликаты:")
            duplicates.forEach { (hash, files) ->
                println("Hash SHA-256 (${hash.take(12)}...):")
                files.forEach { println("   • $it") }
            }
        }
    }
}

fun main() = runBlocking {
    println("=== Задание 2: Поиск дубликатов JSON-файлов ===")

    // автоматическое создание тестовой папки с файлами-дубликатами
    val testDir = File(System.getProperty("java.io.tmpdir"), "task2_demo_files").apply { mkdirs() }

    File(testDir, "user_profile.json").writeText("{\"id\": 10, \"name\": \"Ivan\"}")
    File(testDir, "user_copy.json").writeText("{\"id\": 10, \"name\": \"Ivan\"}")
    File(testDir, "stats.json").writeText("{\"views\": 1500, \"likes\": 300}")

    println("\nТаймаут 5 секунд:")
    val timeSuccess = measureTimeMillis {
        findDuplicates(targetDir = testDir, timeoutSeconds = 5)
    }
    println("Время работы: $timeSuccess мс")

    println("\nТаймаут 0 секунд (проверка отмены):")
    findDuplicates(targetDir = testDir, timeoutSeconds = 0)
}