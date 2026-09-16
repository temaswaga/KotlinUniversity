package com.example.myapplication.Task13

import androidx.activity.ComponentActivity
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
import androidx.lifecycle.ViewModelProvider
import java.util.Locale

@Composable
fun CurrencyScreen() {
    val context = LocalContext.current
    val viewModel: CurrencyViewModel = remember {
        ViewModelProvider(context as ComponentActivity)[CurrencyViewModel::class.java]
    }

    val rate by viewModel.rate.collectAsState()
    val trend by viewModel.trend.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()

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
            Text(
                text = "Курс USD → RUB",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .size(width = 240.dp, height = 180.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE2E7EC)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.2f", rate)} ₽",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        when (trend) {
                            RateTrend.UP -> {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "▲",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            RateTrend.DOWN -> {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "▼",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }
                            RateTrend.SAME -> Unit
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Последнее обновление: $lastUpdated",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.updateRate() },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006684)
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Обновить сейчас",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}