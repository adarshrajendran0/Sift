package com.adarsh.sift

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.adarsh.sift.data.Source
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val text = remoteInput.getCharSequence("capture_text")?.toString()
            if (!text.isNullOrBlank()) {
                val app = context.applicationContext as SiftApplication
                app.applicationScope.launch {
                    try {
                        app.repository.insertItem(text, Source.NOTIFICATION)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        
        // Re-issue the notification so it stays active if enabled
        val prefs = context.getSharedPreferences("sift_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("notification_enabled", false)) {
            NotificationHelper.showNotification(context)
        }
    }
}

object NotificationHelper {
    private const val CHANNEL_ID = "sift_capture"
    const val NOTIFICATION_ID = 1

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Capture",
                NotificationManager.IMPORTANCE_LOW // silent
            ).apply {
                description = "Ongoing notification for capturing thoughts"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context) {
        createChannel(context)

        val remoteInput = RemoteInput.Builder("capture_text")
            .setLabel("Capture a thought")
            .build()

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val action = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Capture",
            pendingIntent
        ).addRemoteInput(remoteInput).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Capture a thought")
            .setOngoing(true)
            .addAction(action)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }
}
