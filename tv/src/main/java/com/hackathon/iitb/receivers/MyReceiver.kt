package com.hackathon.iitb.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hackathon.iitb.model.Show

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        intent ?: return
        val show = intent.getSerializableExtra("show") as Show

        Log.d("suthar", intent.action ?: "null"+" Action")

        if (intent.action?.equals("ignore", true) == true) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val value = Gson().fromJson<ArrayList<Long>>(
                prefs.getString("ignore_list", "[]"),
                object : TypeToken<ArrayList<Long>>() {}.type
            )
            value.add(show.id)

            prefs.edit().putString("ignore", Gson().toJson(value)).apply()

        }
    }
}