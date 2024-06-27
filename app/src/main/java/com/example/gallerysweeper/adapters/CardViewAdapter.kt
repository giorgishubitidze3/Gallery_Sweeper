package com.example.gallerysweeper.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.MediaItem

class CardViewAdapter():RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {

    var list : List<MediaItem> = listOf()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val img = itemView.findViewById<ImageView>(R.id.item_card_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         val currentItem = list[position]


        Glide.with(holder.img.context)
            .load(currentItem.uri)
            .into(holder.img)
    }



    fun setData(list:List<MediaItem>){
        this.list = list
        notifyDataSetChanged()
    }

    fun getItem(position: Int): MediaItem = list[position]

}