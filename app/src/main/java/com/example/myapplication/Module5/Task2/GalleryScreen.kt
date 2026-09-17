package com.example.myapplication.Module5Task2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: GalleryViewModel = remember {
        ViewModelProvider(context as ComponentActivity)[GalleryViewModel::class.java]
    }

    val photos by viewModel.photos.collectAsState()
    var selectedPhotoForExport by remember { mutableStateOf<File?>(null) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onPhotoTaken(success)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createNewPhotoUri()
            pendingUri = uri
            uri?.let { takePictureLauncher.launch(it) }
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val uri = viewModel.createNewPhotoUri()
            pendingUri = uri
            uri?.let { takePictureLauncher.launch(it) }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F8FB),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launchCamera() },
                containerColor = Color(0xFFBAE6FD),
                contentColor = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(size.width / 2f, 4.dp.toPx()),
                        end = Offset(size.width / 2f, size.height - 4.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(4.dp.toPx(), size.height / 2f),
                        end = Offset(size.width - 4.dp.toPx(), size.height / 2f),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (photos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "У вас пока нет фото",
                        fontSize = 17.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Сделайте первое!",
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(photos, key = { it.absolutePath }) { file ->
                        PhotoItem(
                            file = file,
                            onClick = { selectedPhotoForExport = file }
                        )
                    }
                }
            }
        }
    }

    if (selectedPhotoForExport != null) {
        AlertDialog(
            onDismissRequest = { selectedPhotoForExport = null },
            title = { Text("Экспорт фото") },
            text = { Text("Сохранить это фото в системную галерею?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val file = selectedPhotoForExport!!
                        selectedPhotoForExport = null
                        viewModel.exportToGallery(file) { success ->
                            if (success) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Фото добавлено в галерею")
                                }
                            }
                        }
                    }
                ) {
                    Text("Экспортировать", color = Color(0xFF006684))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPhotoForExport = null }) {
                    Text("Отмена", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
fun PhotoItem(file: File, onClick: () -> Unit) {
    var bitmap by remember(file.absolutePath) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(file.absolutePath) {
        withContext(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, options)
                options.inSampleSize = calculateInSampleSize(options, 250, 250)
                options.inJustDecodeBounds = false
                BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }?.let {
            bitmap = it
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color(0xFFE2E7EC), RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}