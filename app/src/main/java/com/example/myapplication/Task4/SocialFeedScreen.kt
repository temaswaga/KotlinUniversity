package com.example.myapplication

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.random.Random

fun loadFeedAssets(context: Context): Pair<List<PostRaw>, List<Comment>> {
    val jsonParser = Json { ignoreUnknownKeys = true }

    val postsText = context.assets.open("social_posts.json").bufferedReader().use { it.readText() }
    val commentsText = context.assets.open("comments.json").bufferedReader().use { it.readText() }

    val posts = jsonParser.decodeFromString<List<PostRaw>>(postsText)
    val comments = jsonParser.decodeFromString<List<Comment>>(commentsText)
    return posts to comments
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var feedJob by remember { mutableStateOf<Job?>(null) }
    var postsState by remember { mutableStateOf<List<PostCardUiState>>(emptyList()) }
    var isReadingAssets by remember { mutableStateOf(false) }

    fun loadFeed() {
        // oтмена всех предыдущих активных корутинов
        feedJob?.cancel()

        feedJob = coroutineScope.launch {
            isReadingAssets = true

            // чтение локальных JSON на пуле Dispatchers.IO
            val (posts, allComments) = withContext(Dispatchers.IO) {
                loadFeedAssets(context)
            }

            postsState = posts.map { PostCardUiState(post = it) }
            isReadingAssets = false

            // supervisorScope изолирует ошибки подзадач постов друг от друга
            supervisorScope {
                posts.forEachIndexed { index, post ->
                    launch {
                        // параллельная загрузка аватарки и комментариев
                        val avatarDeferred = async(Dispatchers.IO) {
                            try {
                                delay(Random.nextLong(600, 1800))
                                if (Random.nextInt(100) < 20) throw RuntimeException("Avatar network error")
                                LoadingState.Success(post.avatarUrl)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                LoadingState.Error(e.message ?: "Avatar error")
                            }
                        }

                        val commentsDeferred = async(Dispatchers.IO) {
                            try {
                                delay(Random.nextLong(800, 2200))
                                if (Random.nextInt(100) < 20) throw RuntimeException("Comments server error")
                                val filtered = allComments.filter { it.postId == post.id }
                                LoadingState.Success(filtered)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                LoadingState.Error(e.message ?: "Comments error")
                            }
                        }


                        val avatarResult = avatarDeferred.await()
                        val commentsResult = commentsDeferred.await()


                        postsState = postsState.toMutableList().also { list ->
                            list[index] = list[index].copy(
                                avatarState = avatarResult,
                                commentsState = commentsResult
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadFeed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Социальная лента") },
                actions = {
                    IconButton(onClick = { loadFeed() }) {
                        Text(text = "🔄", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isReadingAssets && postsState.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(postsState, key = { it.post.id }) { postUi ->
                        PostCard(postUi = postUi)
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(postUi: PostCardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // шапка: аватарка + заголовок поста + бейдж статуса
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AvatarView(state = postUi.avatarState)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = postUi.post.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Автор #${postUi.post.userId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CardStatusBadge(postUi = postUi)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = postUi.post.body,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            CommentsView(state = postUi.commentsState)
        }
    }
}

@Composable
fun AvatarView(state: LoadingState<String>) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is LoadingState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            is LoadingState.Success -> {
                SubcomposeAsyncImage(
                    model = state.data,
                    contentDescription = "Аватар автора",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    },
                    error = {
                        Text(
                            text = "👤",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                )
            }
            is LoadingState.Error -> {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(text = "⚠️", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun CardStatusBadge(postUi: PostCardUiState) {
    val (text, color) = when {
        !postUi.isFullyLoaded -> "Loading" to MaterialTheme.colorScheme.primary
        postUi.hasErrors -> "Error" to MaterialTheme.colorScheme.error
        else -> "Ready" to MaterialTheme.colorScheme.secondary
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CommentsView(state: LoadingState<List<Comment>>) {
    Column {
        Text(
            text = "Комментарии:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        when (state) {
            is LoadingState.Loading -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Загрузка комментариев...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is LoadingState.Success -> {
                if (state.data.isEmpty()) {
                    Text(
                        text = "Нет комментариев",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.data.forEach { comment ->
                        Text(
                            text = "${comment.name}: ${comment.body}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            is LoadingState.Error -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ошибка загрузки комментариев",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}