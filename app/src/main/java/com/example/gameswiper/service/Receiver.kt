package com.example.gameswiper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.e("Powiadomienie", "wysłane")
        NotificationMan.showNotification(
            context,
            "Do you have anything to play?",
            "Haven't seen you for a while. Maybe check Gameswiper? \uD83C\uDFAE"
        )
        Scheduler.scheduleNotification(context, 17, 0)
    }
}