package com.example.myapplication.Module6.Task7

data class BleDeviceItem(
    val name: String,
    val address: String
)

sealed interface ConnectionStatus {
    data object Disconnected : ConnectionStatus
    data object Connecting : ConnectionStatus
    data object Connected : ConnectionStatus
}