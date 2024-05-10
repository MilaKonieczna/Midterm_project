package com.example.tryingmybest.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tryingmybest.R

/**
 * Notify BroadcastReceiver sends a notification to the user when a vaccine alert is received.
 */
class Notify : BroadcastReceiver() {

    /**
     * Called when the Notify is receiving an Intent broadcast.
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        // Getting the vaccine name from the intent received from Add()
        val name = intent.getStringExtra("vaccinations")

        // Building notification with the received vaccine name
        val notificationBuilder = NotificationCompat.Builder(context, "ChannelId")
            .setSmallIcon(R.drawable.virus)
            .setContentTitle("Vaccine alert!")
            .setContentText(name)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Creating notification channel
        val channel = NotificationChannel(
            "ChannelId",
            "ChannelId",
            NotificationManager.IMPORTANCE_HIGH
        )

        NotificationManagerCompat.from(context).createNotificationChannel(channel)

        // Showing the notification
        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.notify(200, notificationBuilder.build())
    }
}
