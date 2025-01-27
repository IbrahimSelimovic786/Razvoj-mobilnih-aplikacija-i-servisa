package com.example.familyhustle

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(private var tasks: MutableList<TaskData>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.taskNameTextView)
        val taskDueDate: TextView = itemView.findViewById(R.id.taskDueDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.name
        holder.taskDueDate.text = "Due: ${task.dueDate}"

        // Promjena boje na osnovu statusa
        if (task.completed) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, TaskDetailActivity::class.java)
            intent.putExtra("TASK_ID", task.id)
            intent.putExtra("TASK_NAME", task.name)
            intent.putExtra("TASK_DESCRIPTION", task.description)
            intent.putExtra("TASK_DUE_DATE", task.dueDate)
            holder.itemView.context.startActivity(intent)
        }
    }



    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskData>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}
