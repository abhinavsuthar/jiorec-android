package com.hackathon.iitb.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hackathon.iitb.model.Product
import com.hackathon.iitb.model.Show

class MyReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {

        intent ?: return
        val show = intent.getSerializableExtra("show") as Show
        val mNotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(show.id.toInt() * 5000)


        Log.d("suthar", intent.action ?: "null"+" Action")

        if (intent.action?.equals("ignore", true) == true) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val value = Gson().fromJson<ArrayList<Long>>(
                prefs.getString("ignore_list", "[]"),
                object : TypeToken<ArrayList<Long>>() {}.type
            )
            value.add(show.id)

            prefs.edit().putString("ignore_list", Gson().toJson(value)).apply()

        }
    }
}

class MyReceiver2 : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {

        intent ?: return
        val product = intent.getSerializableExtra("product") as Product
        val mNotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(product.id * 100)


        Log.d("suthar", intent.action ?: "null"+" Action")

        if (intent.action?.equals("ignore", true) == true) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val value = Gson().fromJson<ArrayList<Int>>(
                prefs.getString("ignore_list2", "[]"),
                object : TypeToken<ArrayList<Int>>() {}.type
            )
            value.add(product.id)

            prefs.edit().putString("ignore_list2", Gson().toJson(value)).apply()

        }
    }
}