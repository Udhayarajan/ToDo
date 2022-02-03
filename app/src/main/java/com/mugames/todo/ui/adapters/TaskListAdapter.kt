package com.mugames.todo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mugames.todo.data.*
import com.mugames.todo.databinding.TaskItemBinding


class TaskListAdapter(
    private val onTaskItemClicked: (Int, View) -> Unit
) : ListAdapter<TaskWithSubTasks, TaskListAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(
        private val binding: TaskItemBinding,
        private val onTaskItemClicked: (Int, View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(taskWithSubTasks: TaskWithSubTasks) {
            binding.apply {
                taskCard.transitionName = String.format("task_card_%s", (taskWithSubTasks.task.id?.minus(1)))
                taskCard.setOnClickListener {
                    onTaskItemClicked(taskWithSubTasks.task.id?.minus(1)?:0, it)
                }
                title.text = taskWithSubTasks.task.tile
                description.isSelected = true
                description.text = taskWithSubTasks.task.description
                dateDue.text = taskWithSubTasks.task.getDueDate(binding.root.context)
                dateAssigned.text = taskWithSubTasks.task.getAssignedDate(binding.root.context)
                if (taskWithSubTasks.subTasks.isNotEmpty())
                    ((taskWithSubTasks.getSubtaskDoneCount().toDouble() / taskWithSubTasks.subTasks.size.toDouble()) * 100).toInt().also {
                        progress.progress = it
                    }
                else
                    progress.progress = 100
            }
        }

        fun getView(): View {
            return binding.root
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TaskWithSubTasks>() {
            override fun areItemsTheSame(oldItem: TaskWithSubTasks, newItem: TaskWithSubTasks): Boolean {
                return oldItem.task.tile == newItem.task.tile
            }

            override fun areContentsTheSame(oldItem: TaskWithSubTasks, newItem: TaskWithSubTasks): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TaskItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onTaskItemClicked
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}