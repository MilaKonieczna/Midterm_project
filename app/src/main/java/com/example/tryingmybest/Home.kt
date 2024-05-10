package com.example.tryingmybest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tryingmybest.adapters.CalendarAdapter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * This class represents the main screen of the application.
 * It displays a calendar view along with options to navigate to history and upcoming events.
 */
class Home : AppCompatActivity(), CalendarAdapter.OnItemListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDate: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI elements
        initWidgets()

        // Set the current date as the selected date
        selectedDate = LocalDate.now()

        // Display the calendar for the current month
        setMonthView()

        // Set click listeners for navigating to history and upcoming events
        val history = findViewById<LinearLayout>(R.id.HistoryLL)
        val upcoming = findViewById<LinearLayout>(R.id.UpcomingLL)
        history.setOnClickListener{goToHistory()}
        upcoming.setOnClickListener{goToUpcoming()}
    }

    /**
     * Initializes User Interface elements.
     */
    private fun initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearTV)

        // Set click listeners for navigating to previous and next months
        findViewById<View>(R.id.previousMonthBTN).setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            setMonthView()
        }
        findViewById<View>(R.id.nextMonthBTN).setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            setMonthView()
        }
    }

    /**
     * Sets up the calendar view for the selected month.
     */
    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate)
        val daysInMonth = daysInMonthArray(selectedDate)

        val calendarAdapter = CalendarAdapter(daysInMonth, this)
        val layoutManager = GridLayoutManager(this, 7)
        calendarRecyclerView.layoutManager = layoutManager
        calendarRecyclerView.adapter = calendarAdapter
    }

    /**
     * Generates an array representing the days in the selected month.
     */
    private fun daysInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstOfMonth = selectedDate.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value

        for (i in 1..42) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("")
            } else {
                daysInMonthArray.add((i - dayOfWeek).toString())
            }
        }
        return daysInMonthArray
    }

    /**
     * Formats the date to display month and year.
     */
    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }

    /**
     * Handles item click events from the calendar adapter.
     */
    override fun onItemClick(position: Int, dayText: String) {
        if (dayText.isNotEmpty()) {
            selectedDate = LocalDate.of(selectedDate.year, selectedDate.month, dayText.toInt())
        }
    }

    /**
     * Goes to the History screen.
     */
    private fun goToHistory() {
        val intent = Intent(this, History::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Goes to the Upcoming screen.
     */
    private fun goToUpcoming() {
        val intent = Intent(this, Upcoming::class.java)
        startActivity(intent)
        finish()
    }
}
