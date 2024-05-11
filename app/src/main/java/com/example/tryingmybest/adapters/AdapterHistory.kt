package com.example.tryingmybest.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tryingmybest.R
import com.example.tryingmybest.data.DataVaxx
import java.text.SimpleDateFormat
import java.util.Locale
/**
 * Adapter for displaying vaccination history in a RecyclerView.
 * This adapter takes a list of [DataVaxx] objects and displays them in the RecyclerView.
 * It also provides filtering functionality to search for specific vaccine names.
 * @param historyList The list of vaccination data to be displayed.
 */
class AdapterHistory(private val historyList: List<DataVaxx>) :
    RecyclerView.Adapter<AdapterHistory.HistoryViewHolder>(), Filterable {

        //filtered list to allow for the search view changeable list size
    private var filteredList: List<DataVaxx> = historyList

    /**
     * ViewHolder class for holding views of each item in the RecyclerView.
     */
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var dateNDTV: TextView = itemView.findViewById(R.id.dateND)
        var dateADMTV: TextView = itemView.findViewById(R.id.dateADM)
        var descTV: TextView = itemView.findViewById(R.id.desc)
        var linearLayout: LinearLayout = itemView.findViewById(R.id.stable)
        var adminLayout: LinearLayout = itemView.findViewById(R.id.adminLayout)
        var expendableLayout: RelativeLayout = itemView.findViewById(R.id.Expandable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_vaccine, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item: DataVaxx = filteredList[position]
        holder.name.text = item.name

        // formats
        val nextDoseDate = item.nextDose?.let {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
        }
        holder.dateNDTV.text = nextDoseDate

        // formats when isn't null, make the linear layout invisible when is null.
        if (item.lastDose != null) {
            val lastDoseDate = item.lastDose?.let {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
            }
            holder.adminLayout.visibility = View.VISIBLE
            holder.dateADMTV.text = lastDoseDate
        } else {
            holder.adminLayout.visibility = View.GONE
        }

        holder.descTV.text = item.desc

        val isExpandable: Boolean = historyList[position].expandable
        holder.expendableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

        holder.linearLayout.setOnClickListener {
            val version = historyList[position]
            version.expandable = !item.expandable
            notifyItemChanged(position)
        }
    }

    /**
     * return the size of the List
     */
    override fun getItemCount(): Int {
        return filteredList.size
    }

    /**
     * filter the items by the name
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                filteredList = if (charString.isEmpty()) {
                    historyList
                } else {
                    historyList.filter { item ->
                        item.name.lowercase(Locale.getDefault()).contains(
                            charString.lowercase(
                                Locale.getDefault()
                            )
                        )
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                filteredList = filterResults?.values as? List<DataVaxx> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}
