package com.example.familyhustle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeekAdapter(
    private val weeks: List<Week>,
    private val onClick: (Week) -> Unit
) : RecyclerView.Adapter<WeekAdapter.WeekViewHolder>() {

    inner class WeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWeekTitle: TextView = itemView.findViewById(R.id.tvWeekTitle)
        val tvWeekDates: TextView = itemView.findViewById(R.id.tvWeekDates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_week_card, parent, false)
        return WeekViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        val week = weeks[position]
        holder.tvWeekTitle.text = week.title
        holder.tvWeekDates.text = week.dates
        holder.itemView.setOnClickListener { onClick(week) }
    }

    override fun getItemCount(): Int = weeks.size
}
