package com.example.myapplication

import kotlinx.serialization.Serializable

@Serializable
data class PostRaw(
    val id: Long,
    val userId: Long,
    val title: String,
    val body: String,
    val avatarUrl: String
)

@Serializable
data class Comment(
    val postId: Long,
    val id: Long,
    val name: String,
    val body: String
)

sealed interface LoadingState<out T> {
    data object Loading : LoadingState<Nothing>
    data class Success<T>(val data: T) : LoadingState<T>
    data class Error(val message: String) : LoadingState<Nothing>
}

data class PostCardUiState(
    val post: com.example.myapplication.PostRaw,
    val avatarState: com.example.myapplication.LoadingState<String> = _root_ide_package_.com.example.myapplication.LoadingState.Loading,
    val commentsState: com.example.myapplication.LoadingState<List<com.example.myapplication.Comment>> = _root_ide_package_.com.example.myapplication.LoadingState.Loading
) {
    val isFullyLoaded: Boolean
        get() = avatarState !is LoadingState.Loading && commentsState !is LoadingState.Loading

    val hasErrors: Boolean
        get() = avatarState is LoadingState.Error || commentsState is LoadingState.Error
}