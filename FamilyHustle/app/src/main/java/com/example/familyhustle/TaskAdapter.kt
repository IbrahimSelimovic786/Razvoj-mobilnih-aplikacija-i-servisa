package com.example.familyhustle

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

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

        // Promjena boje pozadine ako je zadatak obavljen
        if (task.isCompleted) {
            holder.itemView.setBackgroundColor(Color.GREEN)
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }


    override fun getItemCount(): Int = tasks.size
}
