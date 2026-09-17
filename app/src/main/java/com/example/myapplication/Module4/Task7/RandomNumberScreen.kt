package com.example.myapplication.Module4.Task7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RandomNumberScreen() {
    val context = LocalContext.current
    var isBound by remember { mutableStateOf(false) }
    var currentNumber by remember { mutableStateOf<Int?>(null) }
    var serviceBinder by remember { mutableStateOf<RandomNumberService?>(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? RandomNumberService.LocalBinder
                serviceBinder = binder?.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBinder = null
                isBound = false
                currentNumber = null
            }
        }
    }

    LaunchedEffect(serviceBinder) {
        serviceBinder?.randomNumberFlow?.collectLatest { number ->
            currentNumber = number
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
                isBound = false
            }
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
                text = if (isBound && currentNumber != null) currentNumber.toString() else "—",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (isBound) {
                        context.unbindService(serviceConnection)
                        isBound = false
                        serviceBinder = null
                        currentNumber = null
                    } else {
                        val intent = Intent(context, RandomNumberService::class.java)
                        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006684)
                ),
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = if (isBound) "Отключиться" else "Подключиться",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}