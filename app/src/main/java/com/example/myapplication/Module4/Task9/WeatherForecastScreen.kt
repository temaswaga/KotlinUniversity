package com.example.myapplication.Module4.Task9

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.*

private const val UNIQUE_WEATHER_WORK = "WeatherWorkChain"
private const val TAG_MOSCOW = "tag_moscow"
private const val TAG_LONDON = "tag_london"
private const val TAG_NEWYORK = "tag_newyork"
private const val TAG_REPORT = "tag_report"

@Composable
fun WeatherForecastScreen() {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }

    LaunchedEffect(Unit) {
        WeatherNotificationHelper.createChannel(context)
    }

    val workInfos by workManager
        .getWorkInfosForUniqueWorkFlow(UNIQUE_WEATHER_WORK)
        .collectAsState(initial = emptyList())

    val moscowInfo = workInfos.find { it.tags.contains(TAG_MOSCOW) }
    val londonInfo = workInfos.find { it.tags.contains(TAG_LONDON) }
    val newYorkInfo = workInfos.find { it.tags.contains(TAG_NEWYORK) }
    val reportInfo = workInfos.find { it.tags.contains(TAG_REPORT) }

    val isRunning = workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    val isFinished = reportInfo?.state == WorkInfo.State.SUCCEEDED

    val activeCount = listOf(moscowInfo, londonInfo, newYorkInfo).count {
        it?.state == WorkInfo.State.RUNNING || it?.state == WorkInfo.State.ENQUEUED
    }

    val subtitleText = when {
        isRunning -> "Загрузка... ($activeCount в процессе)"
        isFinished -> "Все данные получены!"
        else -> "Готов начать"
    }

    val subtitleColor = when {
        isRunning -> Color(0xFF006684)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Прогноз погоды",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitleText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = subtitleColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            CityWeatherCard(
                cityName = "Москва",
                workInfo = moscowInfo,
                defaultTemp = "21°C",
                hasStarted = workInfos.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CityWeatherCard(
                cityName = "Лондон",
                workInfo = londonInfo,
                defaultTemp = "-2°C",
                hasStarted = workInfos.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CityWeatherCard(
                cityName = "Нью-Йорк",
                workInfo = newYorkInfo,
                defaultTemp = "-7°C",
                hasStarted = workInfos.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isFinished) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE4E9EC))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Итоговый прогноз:",
                            fontSize = 15.sp,
                            color = Color(0xFF2C383D)
                        )
                        Text(
                            text = "Нью-Йорк: -7°C, дождь\nМосква: 21°C, ясно\nЛондон: -2°C, дождь",
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF2C383D)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Средняя температура: 4°C",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2C383D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isRunning) {
                Button(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFFD3DDE0),
                        disabledContentColor = Color(0xFF88959A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                ) {
                    Text(text = "В процессе...", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        workManager.cancelUniqueWork(UNIQUE_WEATHER_WORK)
                        WeatherNotificationHelper.cancelNotification(context)
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                ) {
                    Text(text = "Отменить", fontSize = 15.sp, color = Color(0xFF006684))
                }
            } else {
                Button(
                    onClick = {
                        WeatherNotificationHelper.showNotification(
                            context,
                            "Загружаем погоду для 3 городов…"
                        )

                        val moscowWork = OneTimeWorkRequestBuilder<CityWeatherWorker>()
                            .setInputData(workDataOf("city_name" to "Москва", "temp" to 21, "condition" to "ясно", "delay_ms" to 2500L))
                            .addTag(TAG_MOSCOW)
                            .build()

                        val londonWork = OneTimeWorkRequestBuilder<CityWeatherWorker>()
                            .setInputData(workDataOf("city_name" to "Лондон", "temp" to -2, "condition" to "дождь", "delay_ms" to 3500L))
                            .addTag(TAG_LONDON)
                            .build()

                        val newYorkWork = OneTimeWorkRequestBuilder<CityWeatherWorker>()
                            .setInputData(workDataOf("city_name" to "Нью-Йорк", "temp" to -7, "condition" to "дождь", "delay_ms" to 4500L))
                            .addTag(TAG_NEWYORK)
                            .build()

                        val reportWork = OneTimeWorkRequestBuilder<WeatherReportWorker>()
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .addTag(TAG_REPORT)
                            .build()

                        workManager
                            .beginUniqueWork(
                                UNIQUE_WEATHER_WORK,
                                ExistingWorkPolicy.REPLACE,
                                listOf(moscowWork, londonWork, newYorkWork)
                            )
                            .then(reportWork)
                            .enqueue()
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006684)),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                ) {
                    Text(text = "Собрать прогноз", fontSize = 15.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CityWeatherCard(
    cityName: String,
    workInfo: WorkInfo?,
    defaultTemp: String,
    hasStarted: Boolean
) {
    val state = workInfo?.state
    val isDone = state == WorkInfo.State.SUCCEEDED
    val isLoading = state == WorkInfo.State.RUNNING || (hasStarted && state == WorkInfo.State.ENQUEUED)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE4E9EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = cityName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E282D)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isDone -> "Готово"
                        isLoading -> "Загружается..."
                        else -> "Ожидание"
                    },
                    fontSize = 14.sp,
                    color = when {
                        isDone -> Color(0xFF006684)
                        else -> Color(0xFF5A666B)
                    }
                )
            }

            when {
                isDone -> {
                    Text(
                        text = defaultTemp,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E282D)
                    )
                }
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = Color(0xFF006684)
                    )
                }
            }
        }
    }
}