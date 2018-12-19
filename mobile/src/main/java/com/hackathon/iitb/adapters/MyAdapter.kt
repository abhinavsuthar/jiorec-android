package com.hackathon.iitb.adapters

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hackathon.iitb.R
import com.hackathon.iitb.activities.DetailsActivity
import com.hackathon.iitb.model.Show


class MyAdapter(private val context: Context, private var list: ArrayList<Show>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.show, p0, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, p: Int) {
        Glide.with(holder.image).load(list[p].image_url).into(holder.image)
        holder.title.text = list[p].name

        holder.parent.setOnClickListener {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra("show", list[p])
            context.startActivity(intent)
        }
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.show_image)
        val title: TextView = view.findViewById(R.id.show_title)
        val parent: ConstraintLayout = view.findViewById(R.id.show_parent)
    }

    fun setShow(list: ArrayList<Show>) {
        this.list = list
        notifyDataSetChanged()
    }
}