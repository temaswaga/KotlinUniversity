package com.example.myapplication.Module6.Task6

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*

interface NobelRepository {
    suspend fun getPrizes(year: String?, category: String?): List<NobelPrize>
}

class NobelRepositoryImpl : NobelRepository {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            gson()
        }
    }

    override suspend fun getPrizes(year: String?, category: String?): List<NobelPrize> {
        // 10.0.2.2:8080 — адрес локального Ktor-сервера для эмулятора Android
        val rawPrizes: List<PrizeServerDto> = client.get("http://10.0.2.2:8080/prizes").body()
        val domainList = rawPrizes.map { it.toDomain() }

        return domainList.filter { prize ->
            val matchYear = year.isNullOrBlank() || prize.year == year.trim()
            val matchCategory = category.isNullOrBlank() || category == "Все" ||
                    prize.category.contains(category.trim(), ignoreCase = true)
            matchYear && matchCategory
        }
    }
}