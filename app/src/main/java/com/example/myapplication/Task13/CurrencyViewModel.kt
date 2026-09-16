package com.example.myapplication.Task13

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round
import kotlin.random.Random

enum class RateTrend {
    UP, DOWN, SAME
}

class CurrencyViewModel : ViewModel() {

    private val _rate = MutableStateFlow(91.25)
    val rate = _rate.asStateFlow()

    private val _trend = MutableStateFlow(RateTrend.SAME)
    val trend = _trend.asStateFlow()

    private val _lastUpdated = MutableStateFlow(getCurrentTime())
    val lastUpdated = _lastUpdated.asStateFlow()

    private var autoUpdateJob: Job? = null

    init {
        startAutoUpdate()
    }

    private fun startAutoUpdate() {
        autoUpdateJob?.cancel()
        autoUpdateJob = viewModelScope.launch {
            while (isActive) {
                delay(5000L)
                updateRate()
            }
        }
    }

    fun updateRate() {
        val newRate = round((88.5 + Random.nextDouble() * 4.0) * 100.0) / 100.0
        val currentRate = _rate.value

        _trend.value = when {
            newRate > currentRate -> RateTrend.UP
            newRate < currentRate -> RateTrend.DOWN
            else -> RateTrend.SAME
        }

        _rate.value = newRate
        _lastUpdated.value = getCurrentTime()
    }

    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}