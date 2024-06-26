package com.example.gallerysweeper.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.YearGroup

class AllMediaAdapter(): RecyclerView.Adapter<AllMediaAdapter.ViewHolder>() {

    var list : List<YearGroup> = listOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val year = itemView.findViewById<TextView>(R.id.tv_rv_item_year)
        val childRecyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_view_item_inner)
        private val monthAdapter = MonthAdapter()

        fun bind(yearGroup: YearGroup) {
            year.text = yearGroup.year.toString()
            childRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            childRecyclerView.adapter = monthAdapter
            monthAdapter.list = yearGroup.months
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_dates,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        holder.bind(currentItem)

    }

    fun setData(list:List<YearGroup>){
        this.list = list
        notifyDataSetChanged()
    }
}