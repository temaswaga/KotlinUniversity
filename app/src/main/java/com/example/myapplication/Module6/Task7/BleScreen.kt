package com.example.myapplication.Module6.Task7

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BleApp() {
    val context = LocalContext.current
    val viewModel: BleViewModel = remember {
        ViewModelProvider(context as ComponentActivity)[BleViewModel::class.java]
    }

    val devices by viewModel.scannedDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()
    val heartRate by viewModel.heartRate.collectAsState()

    // остальной код экрана без изменений...

    // Проверка и запрос разрешений на Android 12+
    var hasPermissions by remember {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        mutableStateOf(permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermissions = result.values.all { it }
        if (hasPermissions) viewModel.startScanAfterGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BLE Scanner",
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E1E1E)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (hasPermissions) {
                    if (isScanning) viewModel.restartScan() else viewModel.toggleScan()
                } else {
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN
                        )
                    }
                    launcher.launch(permissions)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02688B)),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text(
                text = if (isScanning) "Перезапустить сканирование" else "Начать сканирование",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Отображение частоты пульса согласно заданию
        Text(
            text = if (heartRate != null) "Heart Rate: $heartRate bpm" else "Heart Rate: —",
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1E1E1E)
        )

        Spacer(modifier = Modifier.height(6.dp))

        val statusText = when (status) {
            ConnectionStatus.Disconnected -> "Disconnected"
            ConnectionStatus.Connecting -> "Connecting"
            ConnectionStatus.Connected -> "Connected"
        }
        Text(
            text = "Статус: $statusText",
            fontSize = 17.sp,
            color = Color(0xFF2B2B2B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(devices, key = { it.address }) { device ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE3EA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.connectDevice(device.address) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = device.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = device.address,
                            fontSize = 13.sp,
                            color = Color(0xFF4A4A4A)
                        )
                    }
                }
            }
        }

        if (status is ConnectionStatus.Connected) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.refreshData() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02688B)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Обновить данные")
                }

                Button(
                    onClick = { viewModel.disconnect() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02688B)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Отключиться")
                }
            }
        }
    }
}

private fun BleViewModel.startScanAfterGranted() {
    toggleScan()
}