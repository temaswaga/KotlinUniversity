package com.example.myapplication.Module6.Task1

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class PhotoDto(
    @SerializedName("id") val id: String,
    @SerializedName("author") val author: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("url") val url: String,
    @SerializedName("download_url") val downloadUrl: String
)

data class Photo(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val downloadUrl: String
)

fun PhotoDto.toDomain() = Photo(
    id = id,
    author = author,
    width = width,
    height = height,
    downloadUrl = downloadUrl
)

interface PicsumApi {
    @GET("v2/list")
    suspend fun getPhotos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): List<PhotoDto>
}