package com.example.myapplication.Module4.Task11

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (_root_ide_package_.com.example.myapplication.Module4.Task11.ReminderManager.isEnabled(context)) {
                _root_ide_package_.com.example.myapplication.Module4.Task11.ReminderManager.schedulePillReminder(context)
            }
        }
    }
}