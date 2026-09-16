package com.example.myapplication.Task6

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerScreen() {
    val context = LocalContext.current
    var secondsText by remember { mutableStateOf("30") }

    // Запрос разрешения на показ уведомлений для Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Поле ввода количества секунд
            OutlinedTextField(
                value = secondsText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        secondsText = input
                    }
                },
                label = { Text("Секунды") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(0.75f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка «Запустить таймер»
            Button(
                onClick = {
                    val seconds = secondsText.toLongOrNull() ?: 0L
                    if (seconds > 0) {
                        val serviceIntent = Intent(context, TimerService::class.java).apply {
                            putExtra(TimerService.EXTRA_SECONDS, seconds)
                        }
                        context.startService(serviceIntent)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006684)
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Запустить таймер",
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}