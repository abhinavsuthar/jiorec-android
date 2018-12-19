package com.hackathon.iitb.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hackathon.iitb.model.Show
import android.support.v4.app.NotificationCompat
import com.hackathon.iitb.R
import java.io.IOException
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.util.Base64
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hackathon.iitb.activities.DetailsActivity
import com.hackathon.iitb.model.password
import com.hackathon.iitb.model.server
import com.hackathon.iitb.model.username
import com.hackathon.iitb.receivers.MyReceiver
import org.jetbrains.anko.runOnUiThread
import java.net.HttpURLConnection
import java.net.URL


class UpdateRecommendationsService : IntentService("UpdateRecommendationsService") {

    private val MAX_RECOMMENDATIONS = 3
    private val mNotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onHandleIntent(intent: Intent?) {

        Log.d("suthar", "onHandleIntent")

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val value = Gson().fromJson<ArrayList<Long>>(
            prefs.getString("ignore_list", "[]"),
            object : TypeToken<ArrayList<Long>>() {}.type
        )

        val base64 = Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Basic $base64")
        val x: Request = Fuel.get("http://$server/api/recommendation/shows/?format=json")
        x.response { _, _, result ->

            val z = String(result.get())
            Log.d("suthar", "Body: $z")
            val recommendations = Gson().fromJson<ArrayList<Show>>(z, object : TypeToken<ArrayList<Show>>() {}.type)

            for ((count, show) in recommendations.withIndex()) {

                if (value.contains(show.id)) {
                    Log.d("suthar", "Ignored Show")
                    continue
                }

                val image = getBitmapFromURL(show.image_url)

                runOnUiThread {
                    val priority = MAX_RECOMMENDATIONS - count

                    val builder =
                        NotificationCompat.Builder(applicationContext, "What's New")
                            .setContentTitle(show.name)
                            .setContentText("Watch Now")
                            .setPriority(priority)
                            .setLocalOnly(true)
                            .setAutoCancel(true)
                            .setColor(applicationContext.resources.getColor(R.color.colorAccent))
                            .setCategory(Notification.CATEGORY_RECOMMENDATION)
                            .setLargeIcon(image)
                            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentIntent(buildPendingIntent(show))
                            .addAction(
                                android.R.drawable.alert_light_frame,
                                "Ignore",
                                buildPendingIntent("ignore", show)
                            )
                            .addAction(
                                android.R.drawable.alert_light_frame,
                                "Remind Later",
                                buildPendingIntent("remind", show)
                            )

                    val notification = builder.build()
                    mNotificationManager.notify(show.id.toInt() * 5000, notification)
                }

                if (count + 1 >= MAX_RECOMMENDATIONS) {
                    break
                }
            }
        }
    }

    private fun buildPendingIntent(show: Show): PendingIntent {
        val detailsIntent = Intent(this, DetailsActivity::class.java)
        detailsIntent.putExtra("show", show)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(DetailsActivity::class.java)
        stackBuilder.addNextIntent(detailsIntent)
        // Ensure a unique PendingIntents, otherwise all
        // recommendations end up with the same PendingIntent
        detailsIntent.action = java.lang.Long.toString(show.id)

        return stackBuilder.getPendingIntent(show.id.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildPendingIntent(action: String, show: Show): PendingIntent {
        val intent = Intent(this, MyReceiver::class.java)
        intent.action = action
        intent.putExtra("show", show)
        return PendingIntent.getBroadcast(this, show.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }
}
