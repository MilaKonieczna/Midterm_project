package com.example.tryingmybest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.tryingmybest.notifications.Notifications
import com.example.tryingmybest.notifications.Notify
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Add Dialog is a Fragment in which the data is combined,
 * the hardcoded information about the vaccines a and the inputted appointment data.
 *
 */
class Add : DialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var name: String
    private lateinit var dropdownMenu: Spinner
    private var isVaccineChosen = false
    private lateinit var saveButton: Button
    private lateinit var date: TextView
    private lateinit var addNotifTV: TextView
    private lateinit var setAlarmLayout: View
    private lateinit var showFormat: SimpleDateFormat
    private lateinit var sendDateFormat: SimpleDateFormat
    private lateinit var fullFormat: SimpleDateFormat
    private lateinit var fullSendFormat: SimpleDateFormat
    private var sendDate: String = ""
    private var sendNoti: String = ""
    private var lastDate: String = ""
    private var desc: String = ""
    private var doses: Long = 0
    private var duration: Long = 0
    private lateinit var email: String

    /**
     * Initializes the view of the dialog fragment.
     */

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add, container, false)

        // Getting the current user with the firebase authentication.
        val user = FirebaseAuth.getInstance().currentUser
        email = user?.email.toString()

        dropdownMenu = view.findViewById(R.id.dropdownMenu)
        date = view.findViewById(R.id.date)
        addNotifTV = view.findViewById(R.id.add_notif)
        setAlarmLayout = view.findViewById(R.id.SetAlarm)

        // Establishing date formats.
        fullSendFormat = SimpleDateFormat("dd-MM-yyyy, hh:mm", Locale.getDefault())
        fullFormat = SimpleDateFormat("EEE, MMM dd, yyyy hh:mm", Locale.getDefault())
        showFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        sendDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        // Initial text for the text views.
        date.text = "Set appointment date"
        addNotifTV.text = "Set notification date"

        // Set up dropdown menu.
        dropdown()

        // Initializing the date picker for individual date selection.
        date.setOnClickListener {
            showDatePicker()
        }

        // Opening another dialog called notifications that collects information for individual
        // notification time selection.
        setAlarmLayout.setOnClickListener {
            val alertDialog = Notifications()
            alertDialog.show(parentFragmentManager, "NotificationsDialogFragment")
        }

        // Save button initialization.
        // When pressed it begins with saving the data to the firestore,
        // then scheduling the notifications and finally closing the dialog.
        saveButton = view.findViewById(R.id.save)
        saveButton.setOnClickListener {
            saveToFireStore()
            scheduleNotification(sendNoti)
            dismiss()
        }

        return view
    }

    /**
     * Sets up the dropdown menu to select a vaccine.
     * Retrieves vaccine data from Firestore and populates the dropdown menu with vaccine options.
     * Fetches appointment data for the selected vaccine to determine the latest appointment's next dose.
     */
    private fun dropdown() {
        db.collection("vaccinations")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val options = mutableListOf<String>()
                    val vaccineMap = mutableMapOf<String, DocumentSnapshot>()
                    options.add("Select Vaccine")
                    for (document in task.result) {
                        val optionName = document.getString("name")
                        optionName?.let {
                            options.add(it)
                            vaccineMap[it] = document
                        }
                    }
                    val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, options)
                    adapter.setDropDownViewResource(R.layout.item_dropdown)
                    dropdownMenu.adapter = adapter
                    dropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        @SuppressLint("SimpleDateFormat")
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            isVaccineChosen = position != 0
                            enableSaveButton()
                            name = dropdownMenu.selectedItem.toString()
                            val selectedDocument = vaccineMap[name]
                            if (selectedDocument != null) {
                                doses = selectedDocument.getLong("doses") ?: 0
                                duration = selectedDocument.getLong("duration") ?: 0
                                desc = selectedDocument.getString("desc").toString()

                                // Initialize variables to track latest appointment document
                                var latestNextDose: Date? = null
                                var latestAppointmentDocument: DocumentSnapshot? = null

                                db.collection("appointments")
                                    .whereEqualTo("name", name)
                                    .get()
                                    .addOnCompleteListener { appointmentDocuments ->
                                        if (appointmentDocuments.isSuccessful) {
                                            for (appointmentDocument in appointmentDocuments.result!!) {
                                                val nextDose = appointmentDocument.getDate("nextDose")
                                                if (nextDose != null && (latestNextDose == null || nextDose.after(latestNextDose))) {
                                                    latestNextDose = nextDose
                                                    latestAppointmentDocument = appointmentDocument
                                                    sendDate = nextDose.toString()
                                                }
                                            }

                                            if (latestAppointmentDocument != null) {
                                                val latestND = latestAppointmentDocument!!.getDate("nextDose")
                                                if (latestNextDose != null) {
                                                    lastDate = latestND.toString()
                                                }
                                            }

                                            updateProposedDate()
                                        } else {
                                            println("Error getting documents: ${appointmentDocuments.exception}")
                                        }
                                    }
                            }
                        }
                        // When no vaccine is chosen the button remains disabled
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            isVaccineChosen = false
                            enableSaveButton()
                        }
                    }
                } else {
                    println("Error getting documents: ${task.exception}")
                }
            }
    }


    /**
     * Updates the proposed date for the appointment based on the current date, number of doses,
     * and duration between doses, along with the dynamically fetched `sendDate` value from Firestore.
     * If `sendDate` is null or not set, the proposed date is set to 7 days from the current date.
     * If `sendDate` is available, the proposed date is calculated based on the duration from the `sendDate`.
     * Also, the function sets the notification TV to one day before the proposed date.
     */
    private fun updateProposedDate() {
        val calendar = Calendar.getInstance()

        // Calculate proposed date based on number of doses and duration from sendDate
        if (sendDate.isEmpty()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7)
        } else {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, duration.toInt())
        }

        // Format and display the proposed date
        val proposedDate = showFormat.format(calendar.time)
        date.text = proposedDate
        sendDate = sendDateFormat.format(calendar.time)

        // Set the notification TV to a day before the proposed date
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dayBefore = fullFormat.format(calendar.time)
        addNotifTV.text = dayBefore
    }


    /**
     * Shows a DatePickerDialog to let the user to select a date.
     * The selected date is then formatted and displayed in the UI,
     * and it's stored for further use.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)

                // Format the selected date for display
                val proposedDate = showFormat.format(calendar.time)
                date.text = proposedDate

                // Format the selected date for sending to Firestore
                sendDate = sendDateFormat.format(calendar.time)
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    /**
     * Enables or disables the save button based on whether a vaccine is chosen.
     * Changes background color of the button to green when vaccine is selected.
     */
    private fun enableSaveButton() {
        if (isVaccineChosen) {
            context?.let {
                saveButton.setBackgroundColor(
                    ContextCompat.getColor(
                        it,
                        R.color.green
                    )
                )
            }
            saveButton.isEnabled = true
        } else {
            saveButton.isEnabled = false
        }
    }

    /**
     * Saves appointment data to Firestore, updates vaccination doses, and adds notification data.
     * Appointment data includes name, email, next dose date, last dose date (if available), and description.
     * The function also updates vaccination doses by decrementing the doses count for the corresponding vaccine.
     * Finally, it adds notification data containing the appointment name, date, and email to Firestore.
     */
    private fun saveToFireStore() {
        val appointmentData: HashMap<String, Any> = if (lastDate.isNotEmpty()) {
            hashMapOf(
                "name" to name,
                "email" to email,
                ("nextDose" to sendDateFormat.parse(sendDate)) as Pair<String, Any>,
                ("lastDose" to sendDateFormat.parse(lastDate)) as Pair<String, Any>,
                "desc" to desc
            )
        } else {
            hashMapOf(
                "name" to name,
                "email" to email,
                ("nextDose" to sendDateFormat.parse(sendDate)) as Pair<String, Any>,
                "desc" to desc
            )
        }

        FirebaseFirestore.getInstance().collection("appointments").add(appointmentData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val vaccineRef = db.collection("vaccinations").whereEqualTo("name", name)

                    vaccineRef.get().addOnSuccessListener { querySnapshot ->
                        db.runTransaction { transaction ->
                            for (document in querySnapshot.documents) {
                                val currentDoses = document.getLong("doses") ?: 0
                                val newDoses = currentDoses - 1
                                transaction.update(document.reference, "doses", newDoses)
                            }
                        }.addOnSuccessListener {
                            Log.d(TAG, "Transaction successfully committed.")
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Transaction failed: ", e)
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error getting documents: ", e)
                    }

                } else {
                    Log.e(TAG, "Error saving appointment data: ", task.exception)
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error saving appointment data: ", exception)
            }

        val notificationData = hashMapOf(
            "name" to name,
            "date" to sendNoti,
            "email" to email
        )

        FirebaseFirestore.getInstance().collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { _ ->
                // Handle failure
            }
    }

    /**
     * Sets the selected date and time for notifications and updates the UI accordingly.
     * @param selectedDate The selected date in "dd-MM-yyyy" format.
     * @param selectedTime The selected time.
     */
    @SuppressLint("SetTextI18n")
    fun setSelectedDateTime(selectedDate: String, selectedTime: String) {
        val originalFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
        sendNoti = "$selectedDate, $selectedTime"
        try {
            val dateObj = originalFormat.parse(selectedDate)
            val formattedDate = displayFormat.format(dateObj ?: Date())
            addNotifTV.text = "$formattedDate, $selectedTime"
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    /**
     * Schedules a notification for the specified date and time.
     * @param selectedDateTime The selected date and time in "dd-MM-yyyy, hh:mm" format.
     */
    private fun scheduleNotification(selectedDateTime: String) {
        val intentNot = Intent(requireContext(), Notify::class.java)
        intentNot.putExtra("vaccinations", name)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intentNot,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()

        val formatter = SimpleDateFormat("dd-MM-yyyy, hh:mm", Locale.getDefault())
        val selectedDateTimeMillis = try {
            formatter.parse(selectedDateTime)?.time ?: return
        } catch (e: ParseException) {
            e.printStackTrace()
            return
        }

        calendar.timeInMillis = selectedDateTimeMillis

        val delayMillis = selectedDateTimeMillis - System.currentTimeMillis()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delayMillis,
            pendingIntent
        )
    }
}