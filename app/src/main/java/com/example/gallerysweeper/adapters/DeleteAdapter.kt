package com.example.gallerysweeper.adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.bumptech.glide.Glide
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.MediaItem
import java.util.concurrent.TimeUnit

class DeleteAdapter(val context: Context): RecyclerView.Adapter<DeleteAdapter.ViewHolder>() {

    var list : List<MediaItem> = listOf()
    private val checkedItemList = MutableLiveData<MutableSet<MediaItem>>(mutableSetOf())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val img = itemView.findViewById<ImageView>(R.id.img_delete_list)
        val checkBox = itemView.findViewById<CheckBox>(R.id.button_delete_list_check)
        val videoLength = itemView.findViewById<TextView>(R.id.item_to_delete_video_length)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_delete,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = checkedItemList.value?.contains(currentItem) ?: false

        if(currentItem.isVideo){
            holder.videoLength.text = getVideoDuration(context ,currentItem.uri)
            holder.videoLength.visibility = View.VISIBLE
        }else{
            holder.videoLength.visibility = View.GONE
        }

        holder.checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
            val currentCheckedSet = checkedItemList.value ?: mutableSetOf()

            if (isChecked) {
                currentCheckedSet.add(currentItem)
            } else {
                currentCheckedSet.remove(currentItem)
            }

            checkedItemList.value = currentCheckedSet
        }

        Glide.with(holder.img.context)
            .load(currentItem.uri)
            .into(holder.img)
    }

    fun getCheckedItems(): LiveData<MutableSet<MediaItem>> {
        return checkedItemList
    }

    fun clearCheckedItems() {
        checkedItemList.value?.clear()
        checkedItemList.value = mutableSetOf()
        notifyDataSetChanged()
    }

    fun setData(list:List<MediaItem>){
        this.list =list
        notifyDataSetChanged()
    }

    fun getVideoDuration(context: Context, uri: Uri): String {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillis = time?.toLong() ?: 0
            val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - TimeUnit.HOURS.toMinutes(hours)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(timeInMillis))

            return when {
                hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
                else -> String.format("%02d:%02d", minutes, seconds)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "00:00"
        } finally {
            retriever.release()
        }
    }
}