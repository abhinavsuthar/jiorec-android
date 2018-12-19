package com.hackathon.iitb.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import com.hackathon.iitb.services.UpdateRecommendationsService


class BootupActivity : BroadcastReceiver() {

    private val TAG = "BootupActivity"
    private val INITIAL_DELAY: Long = 5000

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "BootupActivity initiated");
        if (intent?.action?.endsWith(Intent.ACTION_BOOT_COMPLETED) == true) {
            scheduleRecommendationUpdate(context ?: return)
        }
    }

    private fun scheduleRecommendationUpdate(context: Context) {
        Log.d(TAG, "Scheduling recommendations update")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val recommendationIntent = Intent(context, UpdateRecommendationsService::class.java)
        val alarmIntent = PendingIntent.getService(context, 0, recommendationIntent, 0)

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            INITIAL_DELAY,
            AlarmManager.INTERVAL_HALF_HOUR,
            alarmIntent
        )
    }
}