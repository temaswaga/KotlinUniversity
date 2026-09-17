package com.example.myapplication.Module4.Task11

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReminderScreen() {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(_root_ide_package_.com.example.myapplication.Module4.Task11.ReminderManager.isEnabled(context)) }

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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isEnabled) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF006684), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(54.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.5f)
                            lineTo(size.width * 0.45f, size.height * 0.75f)
                            lineTo(size.width * 0.85f, size.height * 0.25f)
                        }
                        drawPath(
                            path = path,
                            color = Color.White,
                            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Напоминание включено",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ежедневно в 20:00",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        _root_ide_package_.com.example.myapplication.Module4.Task11.ReminderManager.cancelReminder(context)
                        isEnabled = false
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Выключить напоминание", fontSize = 15.sp, color = Color.White)
                }
            } else {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path = path, color = Color(0xFFB71C1C))

                    drawLine(
                        color = Color.White,
                        start = Offset(size.width / 2f, size.height * 0.38f),
                        end = Offset(size.width / 2f, size.height * 0.68f),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = Offset(size.width / 2f, size.height * 0.82f)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Напоминание выключено",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Нажмите, чтобы включить",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        _root_ide_package_.com.example.myapplication.Module4.Task11.ReminderManager.schedulePillReminder(context)
                        isEnabled = true
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006684)),
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Включить напоминание в 20:00", fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}