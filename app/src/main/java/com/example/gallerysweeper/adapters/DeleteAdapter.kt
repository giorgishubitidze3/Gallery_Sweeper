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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.bumptech.glide.Glide
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.MediaItem
import java.util.concurrent.TimeUnit

class DeleteAdapter(private val context: Context, private val viewModel: MainViewModel, private val viewLifecycleOwner: LifecycleOwner): RecyclerView.Adapter<DeleteAdapter.ViewHolder>() {

    private var _list: List<MediaItem> = listOf()
    var list: List<MediaItem>
        get() = _list
        set(value) {
            _list = value
            notifyDataSetChanged() // Notify adapter of data changes
            if (_list.isEmpty()) {
                viewModel.setSelectionMode(false)
            }
        }
    private val checkedItemList = MutableLiveData<MutableSet<MediaItem>>(mutableSetOf())



    init {
        viewModel.selectionMode.observe(viewLifecycleOwner) { selectionMode ->
            notifyDataSetChanged()
        }

        checkedItemList.observe(viewLifecycleOwner) {
            notifyDataSetChanged()
        }
    }

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

        viewModel.selectionMode.observe(viewLifecycleOwner){state ->
            if(state){
                holder.checkBox.visibility = View.VISIBLE
            }else{
                holder.checkBox.visibility = View.GONE
            }
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

        holder.itemView.setOnLongClickListener {
            val currentCheckedSet = checkedItemList.value ?: mutableSetOf()

            if(!holder.checkBox.isChecked){
                currentCheckedSet.add(currentItem)
                holder.checkBox.isChecked = true
                viewModel.setSelectionMode(true)
            }

            checkedItemList.value = currentCheckedSet
            true
        }


        holder.itemView.setOnClickListener {
            val currentCheckedSet = checkedItemList.value ?: mutableSetOf()

            if (viewModel.selectionMode.value == true) {
                if (holder.checkBox.isChecked) {
                    currentCheckedSet.remove(currentItem)
                    holder.checkBox.isChecked = false
                } else {
                    currentCheckedSet.add(currentItem)
                    holder.checkBox.isChecked = true
                }

                checkedItemList.value = currentCheckedSet


            }
        }


        Glide.with(holder.img.context)
            .load(currentItem.uri)
            .into(holder.img)

    
    }

    fun getCheckedItems(): LiveData<MutableSet<MediaItem>> {
        return checkedItemList
    }

    fun checkAllItems(){
        val currentCheckedSet = checkedItemList.value ?: mutableSetOf()

        if(currentCheckedSet.size != list.size){
        list.forEach { item ->
            if(!currentCheckedSet.contains(item)){
                currentCheckedSet.add(item)
            }
        }
        }else{
            currentCheckedSet.clear()
        }

        checkedItemList.value = currentCheckedSet
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