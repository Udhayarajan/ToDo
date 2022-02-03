package com.mugames.todo.ui.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.text.Editable
import android.text.TextUtils
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.mugames.todo.R
import com.mugames.todo.ToDoApplication
import com.mugames.todo.data.dateFormat
import com.mugames.todo.data.getSortedSubTasks
import com.mugames.todo.databinding.AddFragmentBinding
import com.mugames.todo.themeColor
import com.mugames.todo.ui.activities.MainActivity
import com.mugames.todo.ui.adapters.EditSubTaskListAdapter
import com.mugames.todo.ui.adapters.SubTaskListAdapter
import com.mugames.todo.ui.viewmodels.AddViewModel
import com.mugames.todo.ui.viewmodels.TaskViewModel
import com.mugames.todo.ui.viewmodels.TaskViewModelFactory
import com.mugames.todo.vibrateDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddFragment : Fragment() {
    private val navArgs: AddFragmentArgs by navArgs()


    private lateinit var binding: AddFragmentBinding

    private val taskViewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            (activity?.application as ToDoApplication).database.getTaskDao()
        )
    }


    private lateinit var viewModel: AddViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = AddFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[AddViewModel::class.java]

        if (navArgs.taskId != -1) {
            taskViewModel.getTaskAt(navArgs.taskId).observe(viewLifecycleOwner) {
                it?.let {
                    viewModel.task = it.task
                    viewModel.setSubTaskList(it.getSortedSubTasks())
                    updateWithEntry()
                    setupSubTaskRecyclerView()
                }
            }
        }
        setupTopBar()
        setupSubTaskRecyclerView()
        setupTitleTextWatcher()
        setupAnimation()
        binding.due.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) openDatePicker() else {
                isValidDate(binding.due.editableText)
            }
        }
        binding.due.setOnClickListener { openDatePicker() }

        lifecycleScope.launchWhenCreated {
            delay(300)
            (requireActivity() as MainActivity).hideTopBar()
        }
    }

    private fun updateWithEntry() {
        binding.apply {
            viewModel.task?.let { task ->
                topAppBar.title = getString(R.string.edit_task)
                title.setText(task.tile)
                due.setText(task.dateFormat())
                descriptionEditText.setText(task.description)
            }
        }
    }

    private fun setupSubTaskRecyclerView() {
        val onTextChanged: (position: Int, newString: String) -> Unit = { position, newString ->
            viewModel.replaceText(position, newString)
        }

        val adapter = EditSubTaskListAdapter(onTextChanged)

        lifecycle.coroutineScope.launchWhenCreated {
            viewModel.subTasks.collect {
                adapter.submitList(it)
            }
        }
        binding.subTaskRecyclerView.adapter = adapter

        binding.createNewSubtask.setOnClickListener {
            viewModel.addSubTask()
            binding.subTaskRecyclerView.scrollToPosition(adapter.itemCount - 1)
            adapter.createNewClicked()
            lifecycleScope.launchWhenCreated {
                delay(20)
                binding.subTaskRecyclerView.findViewHolderForAdapterPosition(adapter.itemCount - 1)
                    ?.let {
                        if (it is EditSubTaskListAdapter.ViewHolder) {
                            val subText = it.itemView.findViewById<TextInputEditText>(R.id.input)
                            subText.requestFocus()
                            val imm =
                                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(subText, InputMethodManager.SHOW_FORCED)
                        }
                    }
            }
        }
        binding.deleteSubtask.setOnClickListener {
            if (adapter.itemCount > 0) {
                binding.subTaskRecyclerView.scrollToPosition(adapter.itemCount - 2)
                val view = binding.subTaskRecyclerView.getChildAt(adapter.itemCount - 1)
                if (view != null && view.hasFocus()) {
                    view.clearFocus()
                    (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                        view.windowToken,
                        0
                    )
                }
                adapter.removeLast()
                viewModel.removeSubTask()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.snackbar_no_subtask_to_delete),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                viewModel.swapSubTask(from, to)
                adapter.notifyItemMoved(from, to)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val index = viewHolder.adapterPosition
                viewModel.removeSubTaskAt(index)
                adapter.notifyItemRemoved(index)
            }


        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.subTaskRecyclerView)
    }

    private fun setupTitleTextWatcher() {
        binding.title.addTextChangedListener {
            it?.let {
                isValidTitle(it)
                binding.titleLayout.suffixText =
                    (binding.title.text.toString().length.toString() + "/15")
            }
        }
    }

    private fun setupAnimation() {
        enterTransition = MaterialContainerTransform().apply {
            startView = requireActivity().findViewById(R.id.fab)
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
            containerColor = requireContext().themeColor(R.attr.colorSurface)
            startContainerColor = requireContext().themeColor(R.attr.colorSecondary)
            endContainerColor = requireContext().themeColor(R.attr.colorSurface)
        }

        returnTransition = Slide().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
    }

    private fun setupTopBar() {
        binding.topAppBar.title = getString(R.string.add_task)
        binding.topAppBar.setNavigationOnClickListener {
            goBack()
        }
        binding.topAppBar.setOnMenuItemClickListener {
            if (it.itemId == R.id.done) addTask()
            true
        }
    }

    private fun openDatePicker() {
        val onDatePicked = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val calender: Calendar = Calendar.getInstance()
            calender.set(year, month, dayOfMonth)
            val dateString =
                SimpleDateFormat("dd-MMM-yy", Locale.getDefault()).format(calender.time.time)
            binding.due.apply {
                setText(dateString)
                setSelection(dateString.length)
            }
        }
        DatePickerFragment(
            onDatePicked,
            binding.due.text.toString()
        )
            .show(requireActivity().supportFragmentManager, null)
    }

    private fun isValidTitle(editable: Editable): Boolean {
        when {
            editable.length > 15 -> {
                binding.titleLayout.error = getString(R.string.input_error_title_max_len)
                return false
            }
            TextUtils.isEmpty(editable) -> {
                binding.titleLayout.error = getString(R.string.input_error_title_empty)
                return false
            }
            else -> binding.titleLayout.error = null
        }

        return true
    }


    private fun addTask() {
        taskViewModel.apply {
            binding.apply {
                val title = title.editableText
                val due = due.editableText
                if (validateInputs(title, due)) {
                    addTask(
                        viewModel.convertSubTasks(
                            title.toString(),
                            descriptionEditText.text.toString(),
                            due.toString()
                        )
                    )
                    taskViewModel.snackBarMessage =
                        getString(if (navArgs.taskId == -1) R.string.snackbar_added else R.string.snackbar_edited)
                    goBack()
                }
            }
        }

    }

    private fun goBack() {
        requireActivity().currentFocus?.let {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
        requireActivity().onBackPressed()
    }

    private fun validateInputs(title: Editable, date: Editable): Boolean {
        return (isValidTitle(title) && isValidDate(date)).also {
            if (!it) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().vibrateDevice(100, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    requireContext().vibrateDevice(100)
                }
            }
        }
    }

    private fun isValidDate(date: Editable): Boolean {
        if (TextUtils.isEmpty(date)) return true
        val dueDate =
            try {
                SimpleDateFormat("dd-MMM-yy hh:mm:ss", Locale.getDefault()).parse(date.toString()
                    .plus(" 23:59:00"))
                    ?: return false
            } catch (e: ParseException) {
                null
            }
        return dueDate?.let {
            binding.dueLayout.error = null
            true
        } ?: run {
            binding.dueLayout.error = getString(R.string.input_error_date_format)
            false
        }
    }


}