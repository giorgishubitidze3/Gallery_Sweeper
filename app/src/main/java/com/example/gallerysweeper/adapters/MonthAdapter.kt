package com.example.gallerysweeper.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.MonthGroup
import com.example.gallerysweeper.data.YearGroup
import java.text.DateFormatSymbols
import java.time.Year


class MonthAdapter(): RecyclerView.Adapter<MonthAdapter.ViewHolder>() {

    var list : List<MonthGroup> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        val month = itemView.findViewById<TextView>(R.id.tv_inner_item_month)
        val count = itemView.findViewById<TextView>(R.id.tv_inner_item_count)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_dates_inner, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        holder.month.text = getMonthName(currentItem.month)
        holder.count.text = currentItem.items.size.toString()
    }

    private fun getMonthName(month: Int): String {
        return DateFormatSymbols().months[month]
    }

    fun setData(list: List<MonthGroup>){
        this.list = list
        notifyDataSetChanged()
    }
}