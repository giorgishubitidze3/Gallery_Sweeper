package com.example.gallerysweeper.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.AlbumGroup


class AllMediaAdapter(private val navController: NavController, private val context:Context): RecyclerView.Adapter<AllMediaAdapter.ViewHolder>() {


    var list : List<AlbumGroup> = listOf()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val img = itemView.findViewById<ImageView>(R.id.img_album)
        val name = itemView.findViewById<TextView>(R.id.tv_album_name)
        val count = itemView.findViewById<TextView>(R.id.tv_album_count)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_albums,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        Glide.with(holder.img.context)
            .load(currentItem.items[0].uri)
            .into(holder.img)

        holder.name.text = currentItem.name
        holder.count.text = currentItem.items.size.toString()

        holder.itemView.setOnClickListener{
            Toast.makeText(context,"Clicked ${holder.count}",Toast.LENGTH_SHORT).show()
        }
    }

    fun setData(list:List<AlbumGroup>){
        Log.d("AllMediaAdapter", "Setting data with ${list.size} groups")
        this.list = list
        notifyDataSetChanged()
    }
}