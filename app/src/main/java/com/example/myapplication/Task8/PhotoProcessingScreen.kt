package com.example.myapplication.Task8

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.*

private const val UNIQUE_WORK_NAME = "PhotoProcessingWorkChain"
const val TAG_COMPRESS = "tag_compress"
const val TAG_WATERMARK = "tag_watermark"
const val TAG_UPLOAD = "tag_upload"

@Composable
fun PhotoProcessingScreen() {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }

    val workInfoList by workManager
        .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
        .collectAsState(initial = emptyList())

    val isRunning = workInfoList.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    val isFinished = workInfoList.isNotEmpty() && workInfoList.all { it.state == WorkInfo.State.SUCCEEDED }
    val isFailed = workInfoList.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }

    val cloudUrl = remember(workInfoList) {
        val uploadInfo = workInfoList.find { it.tags.contains(TAG_UPLOAD) }
        uploadInfo?.outputData?.getString("cloud_url")
    }

    val statusTitle = when {
        isFinished -> "Фото успешно загружено!"
        isFailed -> "Ошибка при обработке"
        isRunning -> {
            when {
                workInfoList.find { it.tags.contains(TAG_UPLOAD) }?.state == WorkInfo.State.RUNNING -> "Загрузка в облако..."
                workInfoList.find { it.tags.contains(TAG_WATERMARK) }?.state == WorkInfo.State.RUNNING -> "Добавляем водяной знак..."
                workInfoList.find { it.tags.contains(TAG_COMPRESS) }?.state == WorkInfo.State.RUNNING -> "Сжимаем фото..."
                else -> "Запущена обработка..."
            }
        }
        else -> "Готов к обработке"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = statusTitle,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isRunning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(4.dp),
                    color = Color(0xFF006684),
                    trackColor = Color(0xFFD6E4EB)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Это может занять несколько секунд...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            if (isFinished && cloudUrl != null) {
                Text(
                    text = "Ссылка на загруженное фото:\n$cloudUrl",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val initialData = workDataOf("input_path" to "gallery/DCIM/sample_photo.jpg")

                    val compressRequest = OneTimeWorkRequestBuilder<CompressPhotoWorker>()
                        .setInputData(initialData)
                        .addTag(TAG_COMPRESS)
                        .build()

                    val watermarkRequest = OneTimeWorkRequestBuilder<WatermarkWorker>()
                        .addTag(TAG_WATERMARK)
                        .build()

                    val uploadRequest = OneTimeWorkRequestBuilder<UploadPhotoWorker>()
                        .addTag(TAG_UPLOAD)
                        .build()

                    workManager
                        .beginUniqueWork(
                            UNIQUE_WORK_NAME,
                            ExistingWorkPolicy.REPLACE,
                            compressRequest
                        )
                        .then(watermarkRequest)
                        .then(uploadRequest)
                        .enqueue()
                },
                enabled = !isRunning,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006684),
                    disabledContainerColor = Color(0xFFD3DDE0),
                    disabledContentColor = Color(0xFF88959A)
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Начать обработку и загрузку",
                    fontSize = 15.sp,
                    color = if (isRunning) Color(0xFF88959A) else Color.White
                )
            }
        }
    }
}