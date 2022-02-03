package com.mugames.todo.ui.fragments

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.mugames.todo.R
import com.mugames.todo.ToDoApplication
import com.mugames.todo.data.Task
import com.mugames.todo.data.getDueDate
import com.mugames.todo.data.getSortedSubTasks
import com.mugames.todo.data.getSubtaskDoneCount
import com.mugames.todo.databinding.ViewTaskFragmentBinding
import com.mugames.todo.themeColor
import com.mugames.todo.ui.activities.MainActivity
import com.mugames.todo.ui.activities.MainActivity.Companion.TAG
import com.mugames.todo.ui.adapters.SubTaskListAdapter
import com.mugames.todo.ui.viewmodels.TaskViewModel
import com.mugames.todo.ui.viewmodels.TaskViewModelFactory
import com.mugames.todo.ui.viewmodels.ViewTaskViewModel

class ViewTaskFragment : Fragment() {

    private var _binding: ViewTaskFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task

    private val navArgs: ViewTaskFragmentArgs by navArgs()

    private var buttonDrawable: Drawable? = null

    private val taskViewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            (requireActivity().application as ToDoApplication).database.getTaskDao()
        )
    }

    private lateinit var viewModel: ViewTaskViewModel
    private lateinit var adapter: SubTaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            setAllContainerColors(requireActivity().themeColor(R.attr.colorSurface))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ViewTaskFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ViewTaskViewModel::class.java]
        buttonDrawable = binding.markDone.background

        setupSubTaskList()

        taskViewModel.snackBarMessage?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            taskViewModel.snackBarMessage = null
        }

        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    taskViewModel.deleteTask(viewModel.taskWithSubTasks)
                    taskViewModel.snackBarMessage = getString(R.string.snackbar_deleted)
                    requireActivity().onBackPressed()
                }
                R.id.edit -> {
                    viewModel.taskWithSubTasks.task.id?.let { id ->
                        if (!viewModel.taskWithSubTasks.task.isCompleted) {
                            val action =
                                ViewTaskFragmentDirections.actionViewTaskFragmentToAddFragment(id)
                            findNavController().navigate(action)
                        } else {
                            snackbarToastToIncomplete(R.string.snackbar_mark_as_incomplete_to_edit)
                        }
                    }
                }
            }

            true
        }

        taskViewModel.getTaskAt(navArgs.position + 1).observe(this.viewLifecycleOwner) {
            it?.let {
                task = it.task
                viewModel.taskWithSubTasks = it
                if (it.task.isCompleted) binding.markDone.text =
                    requireContext().getString(R.string.mark_as_incomplete)
                setupWithTask()
                adapter.isTaskCompleted = task.isCompleted
                Log.d(TAG, "ViewTask onViewCreated: submitting")
                adapter.submitList(it.getSortedSubTasks())
                refreshCompletedSubTask()
            }
        }

        binding.markDone.setOnClickListener {
            viewModel.taskWithSubTasks.task.isCompleted =
                !viewModel.taskWithSubTasks.task.isCompleted
            taskViewModel.addTask(viewModel.taskWithSubTasks)
            taskViewModel.snackBarMessage =
                getString(
                    if (viewModel.taskWithSubTasks.task.isCompleted) R.string.snackbar_marked_as_complete
                    else R.string.mark_as_incomplete
                )
            requireActivity().onBackPressed()
        }

    }

    private fun snackbarToastToIncomplete(resId: Int) {
        Snackbar.make(
            binding.root,
            getString(resId),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun setupSubTaskList() {
        val checkChanged: (position: Int, isChecked: Boolean) -> Unit = { position, status ->
            if (position == -1) snackbarToastToIncomplete(R.string.snackbar_marked_as_incomplete_to_change) else {
                taskViewModel.updateSubTask(viewModel.updateSubTask(position, status))
                refreshCompletedSubTask()
            }
        }
        adapter = SubTaskListAdapter(checkChanged)
        binding.subTaskRecyclerView.adapter = adapter
    }

    private fun refreshCompletedSubTask() {
        viewModel.taskWithSubTasks.apply {
            (getSubtaskDoneCount() == subTasks.size).also {
                binding.markDone.isEnabled = it
                binding.markDone.background =
                    if (it) ContextCompat.getDrawable(requireContext(), R.drawable.fill_btn)
                    else buttonDrawable
            }
            (binding.markDone.background as LayerDrawable)
                .findDrawableByLayerId(R.id.clip_drawable)
                .level = (getSubtaskDoneCount().toDouble() / subTasks.size * 10000).toInt()
        }
    }

    private fun setupWithTask() {
        binding.topAppBar.title = task.tile
        binding.description.text = task.description
        binding.due.text = task.getDueDate(requireContext())
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}