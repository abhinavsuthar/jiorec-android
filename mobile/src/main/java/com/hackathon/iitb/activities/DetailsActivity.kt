package com.hackathon.iitb.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.bumptech.glide.Glide
import com.hackathon.iitb.R
import com.hackathon.iitb.model.Show
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val show = intent.getSerializableExtra("show") as Show
        title = show.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Glide.with(this).load(show.image_url).into(details_image)
        details_title.text = show.genres.toString()

        details_play.setOnClickListener {
            Toast.makeText(this, "Throw user to JioTv/SetTopBox!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == 16908332) super.onBackPressed()

        return super.onOptionsItemSelected(item)
    }
}


