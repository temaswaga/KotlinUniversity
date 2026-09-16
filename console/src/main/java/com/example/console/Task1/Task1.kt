package com.example.console.Task1

import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

suspend fun fetchUsers(): List<String> {
    delay(1800)
    if (Random.nextInt(100) < 20) {
        throw RuntimeException("Ошибка соединения с сервером пользователей")
    }
    return listOf("Alice", "Bob", "Ivan", "Olga")
}

suspend fun fetchSales(): Map<String, Int> {
    delay(1200)
    if (Random.nextInt(100) < 20) {
        throw RuntimeException("Сервер статистики временно недоступен")
    }
    return mapOf("Coffee" to 1680, "Tea" to 475)
}

suspend fun fetchWeather(): List<String> {
    delay(2500)
    if (Random.nextInt(100) < 20) {
        throw RuntimeException("Таймаут погодного шлюза")
    }
    return listOf("Москва: -18°C", "New York: -5°C", "Tokyo: +11°C")
}

fun main() = runBlocking {
    val totalTime = measureTimeMillis {

        val usersTask = async {
            try {
                Result.success(fetchUsers())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        val salesTask = async {
            try {
                Result.success(fetchSales())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        val weatherTask = async {
            try {
                Result.success(fetchWeather())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        val usersResult = usersTask.await()
        val salesResult = salesTask.await()
        val weatherResult = weatherTask.await()

        println("\n\n--- Результаты ---\n")
        usersResult.fold(
            onSuccess = { println("Пользователи: $it") },
            onFailure = { println("Пользователи: сбой (${it.message})") }
        )
        salesResult.fold(
            onSuccess = { println("Продажи: $it") },
            onFailure = { println("Продажи: сбой (${it.message})") }
        )
        weatherResult.fold(
            onSuccess = { println("Погода: $it") },
            onFailure = { println("Погода: сбой (${it.message})") }
        )
    }

    println("\nВремя выполнения: ${totalTime / 1000} сек (ожидалось ~2.5 сек)\n\n\n")
}