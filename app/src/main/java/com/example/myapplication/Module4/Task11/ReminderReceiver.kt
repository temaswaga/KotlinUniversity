package com.example.myapplication.Module4.Task11

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "pill_reminder_channel"
        const val NOTIFICATION_ID = 301
    }

    override fun onReceive(context: Context, intent: Intent?) {
        showNotification(context)

        if (_root_ide_package_.com.example.myapplication.Module4.Task11.ReminderManager.isEnabled(context)) {
            _root_ide_package_.com.example.myapplication.Module4.Task11.ReminderManager.schedulePillReminder(context)
        }
    }

    private fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о лекарствах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ежедневное напоминание принять таблетку"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Время таблетки!")
            .setContentText("Не забудьте принять таблетку в 20:00")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}