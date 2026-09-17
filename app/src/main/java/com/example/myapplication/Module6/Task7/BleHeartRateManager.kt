package com.example.myapplication.Module6.Task7

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

@SuppressLint("MissingPermission")
class BleHeartRateManager(private val context: Context) {

    companion object {
        val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_CHAR_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CCC_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    private val _scannedDevices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val scannedDevices: StateFlow<List<BleDeviceItem>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate: StateFlow<Int?> = _heartRate.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: "Unknown Device"
            val item = BleDeviceItem(name = name, address = device.address)

            val current = _scannedDevices.value
            if (current.none { it.address == item.address }) {
                _scannedDevices.value = current + item
            }
        }
    }

    fun startScan() {
        _scannedDevices.value = emptyList()
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        _isScanning.value = true
        scanner.startScan(scanCallback)
    }

    fun stopScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        if (_isScanning.value) {
            scanner.stopScan(scanCallback)
            _isScanning.value = false
        }
    }

    fun connect(address: String) {
        stopScan()
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return
        _connectionStatus.value = ConnectionStatus.Connecting

        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionStatus.value = ConnectionStatus.Disconnected
        _heartRate.value = null
    }

    fun readDataManually() {
        val service = bluetoothGatt?.getService(HEART_RATE_SERVICE_UUID)
        val char = service?.getCharacteristic(HEART_RATE_CHAR_UUID)
        if (char != null) {
            bluetoothGatt?.readCharacteristic(char)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionStatus.value = ConnectionStatus.Connected
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionStatus.value = ConnectionStatus.Disconnected
                _heartRate.value = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(HEART_RATE_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(HEART_RATE_CHAR_UUID)
                if (characteristic != null) {
                    // Подписываемся на уведомления (Notify)
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(CCC_DESCRIPTOR_UUID)
                    if (descriptor != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            @Suppress("DEPRECATION")
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }
        }

        // Вызывается при Notify от устройства
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            val rawValue = characteristic.value ?: return
            _heartRate.value = parseHeartRate(rawValue)
        }

        // Вызывается при ручном Read
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                @Suppress("DEPRECATION")
                val rawValue = characteristic.value ?: return
                _heartRate.value = parseHeartRate(rawValue)
            }
        }
    }

    // Декодирование пакета Heart Rate Measurement (Bluetooth SIG)
    private fun parseHeartRate(data: ByteArray): Int {
        if (data.isEmpty()) return 0
        val flags = data[0].toInt()
        val is16Bit = (flags and 0x01) != 0
        return if (is16Bit) {
            if (data.size >= 3) {
                (data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8)
            } else 0
        } else {
            if (data.size >= 2) {
                data[1].toInt() and 0xFF
            } else 0
        }
    }
}