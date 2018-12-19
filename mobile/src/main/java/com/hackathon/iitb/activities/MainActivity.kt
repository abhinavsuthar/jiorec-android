package com.hackathon.iitb.activities

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.support.v7.widget.LinearLayoutManager
import android.util.Base64
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hackathon.iitb.R
import com.hackathon.iitb.adapters.MyAdapter
import com.hackathon.iitb.model.Show
import com.hackathon.iitb.model.password
import com.hackathon.iitb.model.server
import com.hackathon.iitb.model.username
import com.hackathon.iitb.services.AdsService
import com.hackathon.iitb.services.UpdateRecommendationsService
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import org.jetbrains.anko.dip
import java.util.*


class MainActivity : AppCompatActivity() {

    private val INITIAL_DELAY: Long = 5000
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Recommendations"

        fuel(object : com.hackathon.iitb.activities.Fuel {
            override fun onData(data: ArrayList<Show>) {
                this@MainActivity.runOnUiThread {
                    setUpRecyclerView(data)
                }
            }
        })

        startService()
        startService2()

        // test()
    }

    private fun fuel(callback: com.hackathon.iitb.activities.Fuel) {

        val base64 = Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Basic $base64")
        val x: Request = Fuel.get("http://$server/api/recommendation/shows/?format=json")
        x.response { _, _, result ->

            val z = String(result.get())
            Log.d("suthar", "Body: $z")
            val y = Gson().fromJson<ArrayList<Show>>(z, object : TypeToken<ArrayList<Show>>() {}.type)
            callback.onData(y)
        }
    }

    private fun setUpRecyclerView(list: ArrayList<Show>) {
        adapter = MyAdapter(this, list)
        val layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        val dividerItemDecoration = VerticalSpaceItemDecoration(dip(16))
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun startService() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val recommendationIntent = Intent(this, UpdateRecommendationsService::class.java)
        val alarmIntent = PendingIntent.getService(this, 0, recommendationIntent, 0)

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            INITIAL_DELAY,
            AlarmManager.INTERVAL_HALF_HOUR,
            alarmIntent
        )
    }

    private fun startService2() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AdsService::class.java)
        val alarmIntent = PendingIntent.getService(this, 0, intent, 0)

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            INITIAL_DELAY,
            AlarmManager.INTERVAL_HOUR,
            alarmIntent
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.player_mic, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == 16908332) super.onBackPressed()
        else if (item?.itemId == R.id.mic) openMic()
        else if (item?.itemId == R.id.clear) clearSharedPreferences()

        return super.onOptionsItemSelected(item)
    }

    private fun clearSharedPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().clear().apply()
        Toast.makeText(this, "Cleared!", Toast.LENGTH_SHORT).show()
    }


    private fun openMic() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, 101)
        else
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                Log.d("Suthar", result[0])
                voiceCommands(result[0])
            }
        }
    }

    private fun voiceCommands(command: String) {
        Toast.makeText(this, "Command is: $command", Toast.LENGTH_LONG).show()

        if (command.contains("recommendation", true))
            startService(Intent(this, UpdateRecommendationsService::class.java))
        else if (command.contains("products", true) || command.contains(
                "advertisement",
                true
            ) || command.contains("ads", true)
        ) startService(Intent(this, AdsService::class.java))
    }

    private fun test() {
        val netFlixId = "43598743" // <== isn't a real movie id
        val watchUrl = "http://www.netflix.com/watch/$netFlixId"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setClassName("com.netflix.mediaclient", "com.netflix.mediaclient.ui.launch.UIWebViewActivity")
            intent.data = Uri.parse(watchUrl)
            startActivity(intent)
        } catch (e: Exception) {
            // netflix app isn't installed, send to website.
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(watchUrl)
            startActivity(intent)
        }

    }

}

interface Fuel {
    fun onData(data: ArrayList<Show>)
}

class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = verticalSpaceHeight
        outRect.left = verticalSpaceHeight
        outRect.right = verticalSpaceHeight

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = verticalSpaceHeight
        }
    }
}