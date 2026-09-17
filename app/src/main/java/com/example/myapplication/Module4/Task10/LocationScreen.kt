package com.example.myapplication.Module4.Task10

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var isLoading by remember { mutableStateOf(false) }
    var addressText by remember { mutableStateOf<String?>(null) }
    var coordinatesText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Логика получения координат и обратного геокодирования
    @SuppressLint("MissingPermission")
    fun fetchLocationAndAddress() {
        isLoading = true
        errorMessage = null

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                coordinatesText = "Lat: ${String.format(Locale.US, "%.6f", location.latitude)}\nLng: ${String.format(Locale.US, "%.6f", location.longitude)}"

                // Запуск геокодера в фоне
                coroutineScope.launch {
                    val address =
                        _root_ide_package_.com.example.myapplication.Module4.Task10.getReadableAddress(
                            context,
                            location.latitude,
                            location.longitude
                        )
                    isLoading = false
                    if (address != null) {
                        addressText = address
                    } else {
                        addressText = "Не удалось определить адрес по координатам"
                    }
                }
            } else {
                isLoading = false
                errorMessage = "Не удалось определить локацию. Проверьте, включен ли GPS."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            isLoading = false
            errorMessage = "Ошибка получения локации: ${exception.localizedMessage}"
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    // Запрос разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            fetchLocationAndAddress()
        } else {
            Toast.makeText(context, "В доступе к геопозиции отказано", Toast.LENGTH_SHORT).show()
        }
    }

    fun onGetLocationClicked() {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchLocationAndAddress()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF006684))
                Spacer(modifier = Modifier.height(24.dp))
            } else if (addressText != null) {
                // Крупный читаемый адрес
                Text(
                    text = addressText!!,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Координаты Lat / Lng
                if (coordinatesText != null) {
                    Text(
                        text = coordinatesText!!,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                // Начальный текст экрана
                Text(
                    text = "Нажмите кнопку",
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(48.dp))
            }

            // Кнопка «Получить мой адрес»
            Button(
                onClick = { onGetLocationClicked() },
                enabled = !isLoading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006684)
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Получить мой адрес",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// Обратное геокодирование (координаты -> адресная строка)
suspend fun getReadableAddress(context: Context, latitude: Double, longitude: Double): String? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val address = addresses.firstOrNull()?.getAddressLine(0)
                            continuation.resume(address)
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.getAddressLine(0)
            }
        } catch (e: Exception) {
            null
        }
    }
}