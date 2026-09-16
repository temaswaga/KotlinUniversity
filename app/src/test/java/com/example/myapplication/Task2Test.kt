package com.example.myapplication

import kotlinx.coroutines.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import kotlin.system.measureTimeMillis

class Task2Test {

    // Suspend-функция вычисления хэша SHA-256 в пуле ввода-вывода (Dispatchers.IO)
    private suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                // Проверяем, активна ли еще корутина (нужно для мгновенной отмены при таймауте)
                ensureActive()
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    // Функция поиска дубликатов с общим таймаутом
    private suspend fun findDuplicates(targetDir: File, timeoutMs: Long) = coroutineScope {
        println("Сканирование директории: ${targetDir.absolutePath}")

        // 1. Рекурсивный сбор всех .json файлов
        val jsonFiles = targetDir.walkTopDown()
            .filter { it.isFile && it.extension.equals("json", ignoreCase = true) }
            .toList()

        println("Найдено JSON-файлов: ${jsonFiles.size}")
        if (jsonFiles.isEmpty()) {
            println("Файлы для анализа не найдены.")
            return@coroutineScope
        }

        // 2. Параллельный расчет хэшей под общим таймаутом withTimeoutOrNull
        val results = withTimeoutOrNull(timeoutMs) {
            val deferredHashes = jsonFiles.map { file ->
                async(Dispatchers.IO) {
                    val hash = calculateSha256(file)
                    hash to file.absolutePath
                }
            }
            deferredHashes.awaitAll()
        }

        // 3. Обработка результата или таймаута
        if (results == null) {
            println("❌ Поиск прерван по таймауту")
        } else {
            println("\n--- Результаты поиска дубликатов ---")
            val duplicates = results.groupBy({ it.first }, { it.second })
                .filter { it.value.size > 1 }

            if (duplicates.isEmpty()) {
                println("Дубликаты не найдены. Все файлы уникальны.")
            } else {
                duplicates.forEach { (hash, paths) ->
                    println("Группа дубликатов (SHA-256: ${hash.take(12)}...):")
                    paths.forEach { path -> println("  • $path") }
                }
            }
        }
    }

    @Test
    fun testFindDuplicatesSuccess() = runBlocking {
        println("=== Тест 1: Успешный поиск дубликатов ===")

        // Создаем временную папку с тестовыми файлами
        val tempDir = File(System.getProperty("java.io.tmpdir"), "json_test_success").apply { mkdirs() }
        File(tempDir, "user_1.json").writeText("{\"id\": 1, \"name\": \"Alice\"}")
        File(tempDir, "user_2.json").writeText("{\"id\": 2, \"name\": \"Bob\"}")
        File(tempDir, "user_copy.json").writeText("{\"id\": 1, \"name\": \"Alice\"}") // Дубликат user_1

        val time = measureTimeMillis {
            findDuplicates(targetDir = tempDir, timeoutMs = 3000) // Таймаут с запасом (3 секунды)
        }
        println("Время выполнения: ${time} мс\n")
    }

    @Test
    fun testTimeoutCancellation() = runBlocking {
        println("=== Тест 2: Проверка отмены по таймауту ===")

        val tempDir = File(System.getProperty("java.io.tmpdir"), "json_test_timeout").apply { mkdirs() }
        File(tempDir, "big_file.json").writeText("a".repeat(100_000))

        // Задаем намеренно крошечный таймаут (0 мс), чтобы сработала отмена
        findDuplicates(targetDir = tempDir, timeoutMs = 0)
    }
}