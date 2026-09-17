package com.example.myapplication.Module4.Task7

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class RandomNumberService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var numberJob: Job? = null

    private val _randomNumberFlow = MutableStateFlow<Int?>(null)
    val randomNumberFlow = _randomNumberFlow.asStateFlow()

    inner class LocalBinder : Binder() {
        fun getService(): RandomNumberService = this@RandomNumberService
    }

    override fun onBind(intent: Intent?): IBinder {
        startGeneratingNumbers()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopGeneratingNumbers()
        return super.onUnbind(intent)
    }

    private fun startGeneratingNumbers() {
        numberJob?.cancel()
        numberJob = serviceScope.launch {
            while (isActive) {
                // Генерация случайного числа 0..100 каждую секунду
                _randomNumberFlow.value = Random.nextInt(0, 101)
                delay(1000L)
            }
        }
    }

    private fun stopGeneratingNumbers() {
        numberJob?.cancel()
        numberJob = null
        _randomNumberFlow.value = null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}