package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Импортируем именно TimerForegroundService из папки Task5
import com.example.myapplication.Task5.TimerForegroundService

@Composable
fun CounterScreen() {
    val context = LocalContext.current

    // Подключаемся к Flow нашего TimerForegroundService
    val seconds by TimerForegroundService.secondsState.collectAsState()
    val isRunning by TimerForegroundService.isRunningState.collectAsState()

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
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = seconds.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка «Старт»
            Button(
                onClick = {
                    if (!isRunning) {
                        val intent = Intent(context, TimerForegroundService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .width(140.dp)
                    .height(48.dp)
            ) {
                Text("Старт", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка «Стоп»
            Button(
                onClick = {
                    val intent = Intent(context, TimerForegroundService::class.java)
                    context.stopService(intent)
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .width(140.dp)
                    .height(48.dp)
            ) {
                Text("Стоп", fontSize = 16.sp)
            }
        }
    }
}