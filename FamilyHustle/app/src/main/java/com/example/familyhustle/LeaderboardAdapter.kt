package com.example.familyhustle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val items: List<LeaderboardItem>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    inner class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.tvName)
        val pointsText: TextView = itemView.findViewById(R.id.tvPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val member = items[position] // Koristimo items umjesto members
        holder.nameText.text = member.name
        holder.pointsText.text = "${member.points} pts"

        // Isticanje prva tri mjesta
        when (position) {
            0 -> holder.itemView.setBackgroundResource(R.drawable.gold_background) // Prvo mjesto
            1 -> holder.itemView.setBackgroundResource(R.drawable.silver_background) // Drugo mjesto
            2 -> holder.itemView.setBackgroundResource(R.drawable.bronze_background) // Treće mjesto
            else -> holder.itemView.setBackgroundResource(R.drawable.default_background) // Ostala mjesta
        }
    }

    override fun getItemCount(): Int = items.size
}
