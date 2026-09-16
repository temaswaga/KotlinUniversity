package com.example.myapplication.Task5

import kotlinx.coroutines.flow.MutableStateFlow

object TimerState {
    // Хранит текущее количество секунд
    val seconds = MutableStateFlow(0)
}