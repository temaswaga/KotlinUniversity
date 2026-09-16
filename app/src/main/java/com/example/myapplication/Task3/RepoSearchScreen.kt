package com.example.myapplication

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.Task3.Repo
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

fun loadRepositoriesFromAssets(context: Context): List<Repo> {
    val jsonString = context.assets.open("github_repos.json")
        .bufferedReader()
        .use { it.readText() }
    val jsonParser = Json { ignoreUnknownKeys = true }
    return jsonParser.decodeFromString(jsonString)
}

@Composable
fun RepoSearchScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var repositories by remember { mutableStateOf<List<Repo>>(emptyList()) }

    // храним ссылку на текущую корутину поиска для её отмены при новом вводе
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // кэш всех репозиториев
    var allRepositories by remember { mutableStateOf<List<Repo>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            allRepositories = loadRepositoriesFromAssets(context)
            repositories = allRepositories
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery

                // отменяем предыдущую задачу поиска, если пользователь продолжает печатать
                searchJob?.cancel()

                searchJob = coroutineScope.launch {
                    isLoading = true

                    // если за это время придет новый символ то отмена
                    delay(500)

                    val filtered = withContext(Dispatchers.IO) {
                        // имитация задержки
                        delay(300)
                        if (newQuery.isBlank()) {
                            allRepositories
                        } else {
                            allRepositories.filter { repo ->
                                repo.full_name.contains(newQuery, ignoreCase = true) ||
                                        (repo.description?.contains(newQuery, ignoreCase = true) == true) ||
                                        (repo.language?.contains(newQuery, ignoreCase = true) == true)
                            }
                        }
                    }

                    repositories = filtered
                    isLoading = false
                }
            },
            label = { Text("Поиск репозиториев...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // индикатор загрузки
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(repositories, key = { it.id }) { repo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = repo.full_name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        repo.description?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⭐ ${repo.stargazers_count}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            repo.language?.let {
                                Text(
                                    text = "Язык: $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}