package com.example.gallerysweeper.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.MediaItem

class DeleteAdapter(): RecyclerView.Adapter<DeleteAdapter.ViewHolder>() {

    var list : List<MediaItem> = listOf()
    private val checkedItemList = MutableLiveData<MutableSet<MediaItem>>(mutableSetOf())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val img = itemView.findViewById<ImageView>(R.id.img_delete_list)
        val checkBox = itemView.findViewById<CheckBox>(R.id.button_delete_list_check)
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
}