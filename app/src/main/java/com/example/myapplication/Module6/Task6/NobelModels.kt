package com.example.myapplication.Module6.Task6

import com.google.gson.annotations.SerializedName

data class PrizeServerDto(
    @SerializedName("id") val id: Int,
    @SerializedName("year") val year: String,
    @SerializedName("category") val category: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("motivation") val motivation: String,
    @SerializedName("laureates") val laureates: List<LaureateServerDto> = emptyList()
)

data class LaureateServerDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("portion") val portion: String,
    @SerializedName("motivation") val motivation: String
)

data class NobelPrize(
    val id: Int,
    val year: String,
    val category: String,
    val laureatesNames: String,
    val shortMotivation: String,
    val laureates: List<LaureateItem>
)

data class LaureateItem(
    val id: String,
    val name: String,
    val portion: String,
    val motivation: String
)

fun PrizeServerDto.toDomain(): NobelPrize {
    val names = if (laureates.isNotEmpty()) {
        laureates.joinToString(", ") { it.fullName }
    } else "No laureates"

    val shortMotiv = if (motivation.length > 100) motivation.take(97) + "..." else motivation

    return NobelPrize(
        id = id,
        year = year,
        category = category,
        laureatesNames = names,
        shortMotivation = shortMotiv,
        laureates = laureates.map {
            LaureateItem(
                id = it.id.toString(),
                name = it.fullName,
                portion = it.portion,
                motivation = it.motivation
            )
        }
    )
}