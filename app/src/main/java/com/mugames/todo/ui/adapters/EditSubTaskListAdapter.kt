package com.mugames.todo.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mugames.todo.data.SubTask
import com.mugames.todo.databinding.EditableSubTaskItemBinding

class EditSubTaskListAdapter(
    private val onTextChanged: (position: Int, newString: String) -> Unit
) : ListAdapter<SubTask, EditSubTaskListAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SubTask>() {
            override fun areItemsTheSame(
                oldItem: SubTask,
                newItem: SubTask
            ): Boolean {
                return oldItem.position == newItem.position
            }

            override fun areContentsTheSame(
                oldItem: SubTask,
                newItem: SubTask
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    class ViewHolder(
        private val binding: EditableSubTaskItemBinding,

        private val onTextChanged: (position: Int, newString: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: SubTask) {
            binding.apply {
                if (task.position != -1) inputLayout.prefixText = (task.position.plus(1)).toString().plus(".")
                input.setText(task.text)
                input.addTextChangedListener { onTextChanged(adapterPosition, it.toString()) }
            }
        }
    }

    fun createNewClicked() {
        notifyItemRangeInserted(itemCount, 1)
    }

    fun removeLast() {
        notifyItemRemoved(itemCount-1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            EditableSubTaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onTextChanged
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}