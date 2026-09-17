package com.example.myapplication.Module4.Task5

import kotlinx.coroutines.flow.MutableStateFlow

object TimerState {
    val seconds = MutableStateFlow(0)
}