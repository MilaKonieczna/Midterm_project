package com.example.tryingmybest.notifications

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.tryingmybest.Add
import com.example.tryingmybest.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Notifications dialog fragment allows users to set date and time for notifications.
 * Then sends the data to add_dialog.
 * @constructor Creates an instance of Notifications dialog fragment.
 */
class Notifications : DialogFragment() {

    private lateinit var date: TextView
    private var sendDate: String =""
    private lateinit var time: TextView
    private lateinit var showFormat: SimpleDateFormat
    private lateinit var sendDateFormat: SimpleDateFormat
    private lateinit var timeFormat: SimpleDateFormat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.dialog_notifications, container, false)

        val saveButton: Button = view.findViewById(R.id.save)
        date = view.findViewById(R.id.date)
        time = view.findViewById(R.id.time)

        //Initializing the formats
        showFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        sendDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Changed to 24-hour format


        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        date.text = showFormat.format(calendar.time)
        time.text = timeFormat.format(calendar.time)

        //Opening the Date Picker
        date.setOnClickListener {
            showDatePicker()
        }

        //Opening the Time Picker
        time.setOnClickListener {
            showTimePicker()
        }

        /**
         * When the save button is clicked, it retrieves the selected date and time,
         * then sets them in the parent fragment's [Add] instance using [Add.setSelectedDateTime],
         * and dismisses the dialog.
         */        saveButton.setOnClickListener {
            val addDialog = parentFragmentManager.findFragmentByTag("AddDialogFragment") as? Add
            addDialog?.setSelectedDateTime(sendDate, time.text.toString())

            dismiss()
        }

        return view
    }

    /**
     * Shows the date picker dialog.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            date.text = showFormat.format(calendar.time)
            sendDate = sendDateFormat.format(calendar.time)
        },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    /**
     * Shows the time picker dialog.
     */
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            time.text = selectedTime
        },
            hourOfDay,
            minute,
            true
        )
        timePickerDialog.show()
    }

}
