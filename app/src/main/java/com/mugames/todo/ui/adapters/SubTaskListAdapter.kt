package com.mugames.todo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mugames.todo.data.SubTask
import com.mugames.todo.databinding.SubtaskItemBinding

class SubTaskListAdapter(
    private val onCheckChanged: (position: Int, isChecked: Boolean) -> Unit,
) : ListAdapter<SubTask, SubTaskListAdapter.ViewHolder>(DIFF_CALLBACK) {

    var isTaskCompleted: Boolean = false

    class ViewHolder(
        private val binding: SubtaskItemBinding,
        private val isTaskComplete: Boolean,
        private val onCheckChanged: (position: Int, isChecked: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: SubTask) {
            binding.apply {
                competedCheckBox.isChecked = task.isDone
                competedCheckBox.isEnabled = !isTaskComplete
                subTask.text = task.text
                position.text = (task.position.plus(1)).toString().plus(".")
                competedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    onCheckChanged(adapterPosition, isChecked)
                }
            }
        }

        fun swapCheckBoxState() {
            binding.competedCheckBox.apply {
                isChecked = !isChecked
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SubTask>() {
            override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SubtaskItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            isTaskCompleted,
            onCheckChanged
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            if (!isTaskCompleted)
                holder.swapCheckBoxState()
            else
                onCheckChanged(-1, false)
        }
    }
}