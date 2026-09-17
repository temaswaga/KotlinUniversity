package com.example.myapplication.Module6.Task2

import com.google.gson.annotations.SerializedName

// --- DTO ---
data class NobelApiResponseDto(
    @SerializedName("nobelPrizes") val nobelPrizes: List<NobelPrizeDto>?
)

data class NobelPrizeDto(
    @SerializedName("awardYear") val awardYear: String?,
    @SerializedName("category") val category: CategoryDto?,
    @SerializedName("laureates") val laureates: List<LaureateDto>?
)

data class CategoryDto(
    @SerializedName("en") val en: String?
)

data class LaureateDto(
    @SerializedName("id") val id: String?,
    @SerializedName("knownName") val knownName: NameDto?,
    @SerializedName("fullName") val fullName: NameDto?,
    @SerializedName("portion") val portion: String?,
    @SerializedName("motivation") val motivation: MotivationDto?
)

data class NameDto(
    @SerializedName("en") val en: String?
)

data class MotivationDto(
    @SerializedName("en") val en: String?
)

// --- Domain Models ---
data class NobelPrize(
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

fun NobelPrizeDto.toDomain(): NobelPrize {
    val yearStr = awardYear ?: "—"
    val catStr = category?.en ?: "General"
    val list = laureates?.map { dto ->
        LaureateItem(
            id = dto.id ?: "",
            name = dto.knownName?.en ?: dto.fullName?.en ?: "Unknown Laureate",
            portion = dto.portion ?: "1",
            motivation = dto.motivation?.en ?: "No motivation provided..."
        )
    } ?: emptyList()

    val names = if (list.isNotEmpty()) list.joinToString(", ") { it.name } else "No laureates"
    val firstMotiv = list.firstOrNull()?.motivation ?: "No motivation provided..."
    val shortMotiv = if (firstMotiv.length > 100) firstMotiv.take(97) + "..." else firstMotiv

    return NobelPrize(
        year = yearStr,
        category = catStr,
        laureatesNames = names,
        shortMotivation = shortMotiv,
        laureates = list
    )
}