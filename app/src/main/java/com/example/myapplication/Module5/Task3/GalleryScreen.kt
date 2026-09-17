package com.example.myapplication.Module5Task3

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
import androidx.compose.foundation.shape.CircleShape
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
                        PhotoItemWithMenu(
                            file = file,
                            onExport = {
                                viewModel.exportToGallery(file) { success ->
                                    if (success) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Фото добавлено в галерею")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoItemWithMenu(
    file: File,
    onExport: () -> Unit
) {
    var bitmap by remember(file.absolutePath) { mutableStateOf<ImageBitmap?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

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
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable { menuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val centerX = size.width / 2f
                    val radius = 1.8.dp.toPx()
                    drawCircle(Color.White, radius, Offset(centerX, size.height * 0.22f))
                    drawCircle(Color.White, radius, Offset(centerX, size.height * 0.5f))
                    drawCircle(Color.White, radius, Offset(centerX, size.height * 0.78f))
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(Color(0xFFF1F5F9))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Экспорт в галерею",
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    },
                    leadingIcon = {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val r = 2.dp.toPx()
                            val p1 = Offset(size.width * 0.25f, size.height * 0.5f)
                            val p2 = Offset(size.width * 0.75f, size.height * 0.25f)
                            val p3 = Offset(size.width * 0.75f, size.height * 0.75f)

                            drawLine(
                                color = Color(0xFF0F172A),
                                start = p1,
                                end = p2,
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFF0F172A),
                                start = p1,
                                end = p3,
                                strokeWidth = 1.8.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            drawCircle(Color(0xFF0F172A), r, p1)
                            drawCircle(Color(0xFF0F172A), r, p2)
                            drawCircle(Color(0xFF0F172A), r, p3)
                        }
                    },
                    onClick = {
                        menuExpanded = false
                        onExport()
                    }
                )
            }
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