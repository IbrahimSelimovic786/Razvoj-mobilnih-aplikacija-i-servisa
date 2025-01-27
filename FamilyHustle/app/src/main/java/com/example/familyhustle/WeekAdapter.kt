package com.example.familyhustle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeekAdapter(
    private val weeks: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<WeekAdapter.WeekViewHolder>() {

    inner class WeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weekText: TextView = itemView.findViewById(R.id.weekTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_week, parent, false)
        return WeekViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        val week = weeks[position]
        holder.weekText.text = week

        // Postavljanje pozadine za svaki element
        holder.itemView.setBackgroundResource(R.drawable.recycler_item_selector)

        // Postavljanje OnClickListener-a
        holder.itemView.setOnClickListener { onClick(week) }
    }

    override fun getItemCount(): Int = weeks.size
}
