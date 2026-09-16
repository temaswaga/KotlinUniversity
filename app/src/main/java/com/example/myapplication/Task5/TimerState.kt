package com.example.myapplication.Task5

import kotlinx.coroutines.flow.MutableStateFlow

object TimerState {
    val seconds = MutableStateFlow(0)
}