package com.example.fitnessapp

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BADGE_ICON_SMALL
import androidx.core.app.NotificationManagerCompat

class Notification : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context,SelectionActivity::class.java)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(context,0,i, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context!!,"APP")
            .setContentText("📝 It's time to write your daily log entry!")
            .setSmallIcon(R.drawable.streak_icon)
            .setContentTitle("75 Hard Challenge")
            .setAutoCancel(true)
            .setBadgeIconType(BADGE_ICON_SMALL)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(123,builder.build())
    }

}
