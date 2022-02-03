package com.mugames.todo.ui.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragment(
    private val onDatePickerListener: DatePickerDialog.OnDateSetListener,
    private val selectedDate: String,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        if (selectedDate.isNotEmpty())
            try {
                SimpleDateFormat("dd-MMM-yy", Locale.getDefault()).parse(selectedDate)
                    ?.let { date ->
                        calendar.time = date
                    }
            } catch (e: ParseException) {
            }
        return DatePickerDialog(
            requireContext(),
            onDatePickerListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).also {
            it.datePicker.minDate = System.currentTimeMillis()
        }
    }
}