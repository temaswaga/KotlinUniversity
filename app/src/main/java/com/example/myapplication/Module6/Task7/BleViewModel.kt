package com.example.myapplication.Module6.Task7

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class BleViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = BleHeartRateManager(application.applicationContext)

    val scannedDevices: StateFlow<List<BleDeviceItem>> = bleManager.scannedDevices
    val isScanning: StateFlow<Boolean> = bleManager.isScanning
    val connectionStatus: StateFlow<ConnectionStatus> = bleManager.connectionStatus
    val heartRate: StateFlow<Int?> = bleManager.heartRate

    fun toggleScan() {
        if (isScanning.value) {
            bleManager.stopScan()
        } else {
            bleManager.startScan()
        }
    }

    fun restartScan() {
        bleManager.stopScan()
        bleManager.startScan()
    }

    fun connectDevice(address: String) {
        bleManager.connect(address)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun refreshData() {
        bleManager.readDataManually()
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.disconnect()
    }
}