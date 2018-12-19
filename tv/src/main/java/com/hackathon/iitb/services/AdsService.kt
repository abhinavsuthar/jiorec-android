package com.hackathon.iitb.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Base64
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hackathon.iitb.DetailsActivity
import com.hackathon.iitb.R
import com.hackathon.iitb.model.*
import com.hackathon.iitb.receivers.MyReceiver
import org.jetbrains.anko.runOnUiThread

class AdsService : IntentService("AdsService") {

    private val MAX_RECOMMENDATIONS = 3
    private val mNotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onHandleIntent(intent: Intent?) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val value = Gson().fromJson<ArrayList<Int>>(
            prefs.getString("ignore_list", "[]"),
            object : TypeToken<ArrayList<Int>>() {}.type
        )


        val base64 = Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Basic $base64")
        val x: Request = Fuel.get("http://$server/api/recommendation/products/?format=json")
        x.response { _, _, result ->

            val z = String(result.get())
            Log.d("suthar", "Body: $z")
            val products = Gson().fromJson<ArrayList<Product>>(z, object : TypeToken<ArrayList<Product>>() {}.type)

            var count = 0
            for (product in products) {

                if (value.contains(product.id)) {
                    Log.d("suthar", "Ignored Show")
                    continue
                }


                runOnUiThread {

                    val priority = MAX_RECOMMENDATIONS - count

                    val builder =
                        NotificationCompat.Builder(applicationContext, "What's New")
                            .setContentTitle(product.name)
                            .setContentText(product.brand)
                            .setPriority(priority)
                            .setLocalOnly(true)
                            .setAutoCancel(true)
                            .setColor(applicationContext.resources.getColor(R.color.fastlane_background))
                            .setCategory(Notification.CATEGORY_RECOMMENDATION)
                            .setSmallIcon(android.R.drawable.ic_menu_report_image) //Change this icon
                            .setContentIntent(buildPendingIntent(product))
                            .addAction(
                                android.R.drawable.alert_light_frame,
                                "Ignore",
                                buildPendingIntent("ignore", product)
                            )
                            .addAction(
                                android.R.drawable.alert_light_frame,
                                "Remind Later",
                                buildPendingIntent("remind", product)
                            )
                    //.setExtras(extras)


                    val notification = builder.build()
                    mNotificationManager.notify(product.id.toInt(), notification)
                }

                if (++count >= MAX_RECOMMENDATIONS) {
                    break
                }
            }
        }
    }

    private fun buildPendingIntent(product: Product): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(product.link)
        return PendingIntent.getActivity(this, product.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildPendingIntent(action: String, product: Product): PendingIntent {
        val intent = Intent(this, MyReceiver::class.java)
        intent.action = action
        intent.putExtra("product", product)
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}