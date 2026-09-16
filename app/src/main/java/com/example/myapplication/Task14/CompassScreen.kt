package com.example.myapplication.Task14

import android.content.Context
import android.hardware.SensorManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.roundToInt

@Composable
fun CompassScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val viewModel: CompassViewModel = remember {
        ViewModelProvider(context as ComponentActivity)[CompassViewModel::class.java].apply {
            initSensors(sensorManager)
        }
    }

    val azimuth by viewModel.azimuth.collectAsState()
    val hasSensor by viewModel.hasSensor.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startListening(sensorManager)
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopListening(sensorManager)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopListening(sensorManager)
        }
    }

    var targetAngle by remember { mutableStateOf(0f) }

    LaunchedEffect(azimuth) {
        val target = -azimuth
        var diff = (target - targetAngle) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        targetAngle += diff
    }

    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 200),
        label = "compass_needle_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!hasSensor) {
            Text(
                text = "Устройство не поддерживает датчик ориентации",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 48.dp)
            ) {
                Text(
                    text = "Компас",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f

                        drawCircle(
                            color = Color(0xFF1E1E1E),
                            radius = radius
                        )
                        drawCircle(
                            color = Color(0xFF2C2C2C),
                            radius = radius,
                            style = Stroke(width = 4.dp.toPx())
                        )

                        rotate(animatedAngle, pivot = center) {
                            val needleWidth = 14.dp.toPx()
                            val needleLength = radius * 0.75f

                            val northPath = Path().apply {
                                moveTo(center.x, center.y - needleLength)
                                lineTo(center.x + needleWidth / 2f, center.y)
                                lineTo(center.x - needleWidth / 2f, center.y)
                                close()
                            }
                            drawPath(northPath, color = Color(0xFFE53935))

                            val southPath = Path().apply {
                                moveTo(center.x, center.y + needleLength)
                                lineTo(center.x + needleWidth / 2f, center.y)
                                lineTo(center.x - needleWidth / 2f, center.y)
                                close()
                            }
                            drawPath(southPath, color = Color(0xFF2B2B2B))

                            drawCircle(
                                color = Color(0xFF1E1E1E),
                                radius = 4.dp.toPx(),
                                center = center
                            )
                        }
                    }

                    Text(
                        text = "N",
                        color = Color(0xFFE53935),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp)
                    )
                }

                Text(
                    text = "Азимут: ${azimuth.roundToInt()}°",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}