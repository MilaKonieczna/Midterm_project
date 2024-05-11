package com.example.tryingmybest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tryingmybest.R
/**
 * Adapter for displaying a calendar visible in the Home Activity.
 * This adapter takes a list of days in a month and displays them in the RecyclerView.
 * It provides a callback interface [OnItemListener] to handle item click events.
 * @param daysOfMonth List of days in the month to be displayed.
 * @param onItemListener Listener to handle item click events.
 */
class CalendarAdapter(
    private val daysOfMonth: ArrayList<String>,
    private val onItemListener: OnItemListener
) : RecyclerView.Adapter<CalendarViewHolder>() {

    /**
     * Interface for handling item click events in the calendar.
     */
    interface OnItemListener {
        /**
         * Called when a calendar item is clicked.
         * @param position The position of the clicked item.
         * @param dayText The text of the clicked day.
         */
        fun onItemClick(position: Int, dayText: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_calendar, parent, false)
        val layoutParams = view.layoutParams
        layoutParams.height = (parent.height * 0.12).toInt()
        return CalendarViewHolder(view, onItemListener)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.dayOfMonth.text = daysOfMonth[position]
    }

    override fun getItemCount(): Int {
        return daysOfMonth.size
    }
}

/**
 * ViewHolder class for holding views of each item in the calendar RecyclerView.
 * @param itemView The view of the item.
 * @param onItemListener Listener to handle item click events.
 */
class CalendarViewHolder(
    itemView: View,
    private val onItemListener: CalendarAdapter.OnItemListener
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val dayOfMonth: TextView = itemView.findViewById(R.id.day)

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        onItemListener.onItemClick(adapterPosition, dayOfMonth.text.toString())
    }
}
