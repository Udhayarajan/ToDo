package com.mugames.todo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.helper.widget.MotionPlaceholder
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.platform.MaterialElevationScale
import com.mugames.todo.R
import com.mugames.todo.ToDoApplication
import com.mugames.todo.data.TaskWithSubTasks
import com.mugames.todo.data.getSubtaskDoneCount
import com.mugames.todo.databinding.ToDoFragmentBinding
import com.mugames.todo.ui.adapters.TaskListAdapter
import com.mugames.todo.ui.activities.MainActivity
import com.mugames.todo.ui.activities.MainActivity.Companion.TAG
import com.mugames.todo.ui.viewmodels.TaskViewModel
import com.mugames.todo.ui.viewmodels.TaskViewModelFactory

class ToDoFragment : Fragment() {
    private var _binding: ToDoFragmentBinding? = null
    private val binding get() = _binding!!

    private val navArgs: ToDoFragmentArgs by navArgs()

    private lateinit var cachedList: List<TaskWithSubTasks>

    val type get() = navArgs.loadData

    private var placeholderId: Int = R.string.list_empty_to_do

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            (activity?.application as ToDoApplication).database.getTaskDao()
        )
    }

    private val taskListAdapter = TaskListAdapter { id, v ->
        onTaskClicked(id, v)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = ToDoFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        binding.taskRecyclerView.adapter = taskListAdapter
        loadEntries()
    }

    fun filter(text: String?) {
        if (text == null) return

        val filteredList = mutableListOf<TaskWithSubTasks>()
        cachedList.forEach {
            if (it.task.tile.contains(text, true) || (it.task.description.contains(text,
                    true))
            ) filteredList.add(it)
        }
        if (text == "") {
            filteredList.clear()
            filteredList.addAll(cachedList)
        }
        if (filteredList.isEmpty()) updateViewPlaceHolder(R.string.list_empty_search,
            text) else updateViewPlaceHolder(null)
        taskListAdapter.submitList(sortList(filteredList))
    }

    private fun updateViewPlaceHolder(id: Int?, additionalString: String? = null) {
        binding.apply {
            id?.let {
                val tempString = getString(it)
                taskRecyclerView.visibility = View.GONE
                placeHolder.visibility = View.VISIBLE
                additionalString?.let {
                    tempString.plus(" ").plus(additionalString)
                }
                placeHolder.text = tempString

            } ?: run {
                placeHolder.visibility = View.GONE
                taskRecyclerView.visibility = View.VISIBLE
                placeHolder.text = ""
            }
        }
    }

    fun menuAction(id: Int) {
        if (id == R.id.clear_list) {
            taskListAdapter.currentList.forEach {
                viewModel.deleteTask(it)
            }
        } else {
            viewModel.sortingId = id
            taskListAdapter.submitList(sortList(taskListAdapter.currentList))
        }
    }

    private fun sortList(currentList: List<TaskWithSubTasks>): List<TaskWithSubTasks> {
        return when (viewModel.sortingId) {
            R.id.sort_by -> currentList
            R.id.sort_due -> currentList.sortedWith(compareBy(nullsLast()) {
                it.task.due
            })
            R.id.sort_completion -> currentList.sortedWith(compareByDescending {
                it.getSubtaskDoneCount()
            })
            else -> currentList.sortedBy { it.task.assigned }
        }
    }

    private fun loadEntries() {
        when (navArgs.loadData) {
            MainActivity.FRAGMENT_TODO -> viewModel.getToDoTask()
                .observe(this.viewLifecycleOwner, this::populateView)
            MainActivity.FRAGMENT_MISSING -> {
                placeholderId = R.string.list_empty_over_due
                viewModel.getOverDueTask()
                    .observe(this.viewLifecycleOwner, this::populateView)
            }
            MainActivity.FRAGMENT_DONE -> {
                placeholderId = R.string.list_empty_done
                viewModel.getDoneTask()
                    .observe(this.viewLifecycleOwner, this::populateView)
            }
        }
    }


    private fun populateView(tasks: List<TaskWithSubTasks>) {
        val sortedList = sortList(tasks)
        taskListAdapter.submitList(sortedList)
        cachedList = sortedList
        viewModel.adapterSize = tasks.size
        binding.taskRecyclerView.layoutManager?.onRestoreInstanceState(viewModel.taskListState)
        if (tasks.isNotEmpty()) updateViewPlaceHolder(null, null) else updateViewPlaceHolder(placeholderId, null)
        startPostponedEnterTransition()
    }

    private fun onTaskClicked(id: Int, v: View) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        val action = ToDoFragmentDirections.actionToDoFragmentToViewTaskFragment(id)
        val extras = FragmentNavigatorExtras(v to getString(R.string.view_description))
        viewModel.taskListState = binding.taskRecyclerView.layoutManager?.onSaveInstanceState()
        viewModel.selectedId = id
        findNavController().navigate(action, extras)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

