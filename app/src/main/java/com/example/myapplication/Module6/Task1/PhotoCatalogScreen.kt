package com.example.myapplication.Module6.Task1

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun PhotoCatalogApp() {
    val context = LocalContext.current
    val viewModel: PhotoViewModel = remember {
        androidx.lifecycle.ViewModelProvider(context as androidx.activity.ComponentActivity)[PhotoViewModel::class.java]
    }
    val uiState by viewModel.uiState.collectAsState()
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9FB)
    ) {
        if (selectedPhoto != null) {
            PhotoDetailScreen(
                photo = selectedPhoto!!,
                onBack = { selectedPhoto = null }
            )
        } else {
            when (val state = uiState) {
                is PhotosUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3F51B5))
                    }
                }
                is PhotosUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = Color(0xFFD32F2F),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadPhotos() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Повторить", fontSize = 16.sp)
                            }
                        }
                    }
                }
                is PhotosUiState.Success -> {
                    PhotoGridScreen(
                        photos = state.photos,
                        onPhotoClick = { selectedPhoto = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGridScreen(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Photos",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1F1F1F)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F9FB)
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoCardItem(photo = photo, onClick = { onPhotoClick(photo) })
            }
        }
    }
}

@Composable
fun PhotoCardItem(
    photo: Photo,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E4E9)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            AsyncImage(
                model = photo.downloadUrl, // Загружаем напрямую картинку
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Text(
                text = photo.author,
                fontSize = 15.sp,
                color = Color(0xFF1F1F1F),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photo: Photo,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        URL(photo.downloadUrl).openStream().use { input ->
                            context.contentResolver.openOutputStream(uri)?.use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    Toast.makeText(context, "Фото сохранено", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фото", fontSize = 20.sp, color = Color(0xFF1F1F1F)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF1F1F1F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AsyncImage(
                model = photo.downloadUrl,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Автор: ${photo.author}",
                    fontSize = 17.sp,
                    color = Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Размер: ${photo.width}×${photo.height}",
                    fontSize = 17.sp,
                    color = Color(0xFF1F1F1F)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { saveFileLauncher.launch("photo_${photo.id}.jpg") },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Скачать фото", fontSize = 16.sp)
                }
            }
        }
    }
}