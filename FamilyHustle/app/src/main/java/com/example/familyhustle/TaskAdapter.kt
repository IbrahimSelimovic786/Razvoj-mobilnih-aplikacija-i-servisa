package com.example.familyhustle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TaskAdapter(private val tasks: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvPoints: TextView = itemView.findViewById(R.id.tvTaskPoints)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTitle.text = task.title
        holder.tvPoints.text = task.points
        holder.tvDescription.text = task.description
    }

    override fun getItemCount(): Int = tasks.size
}
