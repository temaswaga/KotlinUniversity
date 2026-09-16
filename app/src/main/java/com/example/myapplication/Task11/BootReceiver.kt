package com.example.myapplication.Task11

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (ReminderManager.isEnabled(context)) {
                ReminderManager.schedulePillReminder(context)
            }
        }
    }
}