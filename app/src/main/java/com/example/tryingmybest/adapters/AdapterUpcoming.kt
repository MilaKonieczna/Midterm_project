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
 * Adapter for displaying vaccination upcoming in a RecyclerView.
 * This adapter takes a list of [DataVaxx] objects and displays them in the RecyclerView.
 * It also provides filtering functionality to search for specific vaccine names.
 * @param upcomingList The list of vaccination data to be displayed.
 */
class AdapterUpcoming(private val upcomingList: List<DataVaxx>):
    RecyclerView.Adapter<AdapterUpcoming.UpcomingViewHolder>(), Filterable {
    private var filteredList: List<DataVaxx> = upcomingList

    /**
     * ViewHolder class for holding views of each item in the RecyclerView.
     */
    class UpcomingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var dateNDTV: TextView = itemView.findViewById(R.id.dateND)
        var dateADMTV: TextView = itemView.findViewById(R.id.dateADM)
        var descTV: TextView = itemView.findViewById(R.id.desc)
        var linearLayout: LinearLayout = itemView.findViewById(R.id.stable)
        var adminLayout: LinearLayout = itemView.findViewById(R.id.adminLayout)
        var expendableLayout: RelativeLayout = itemView.findViewById(R.id.Expandable)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_vaccine, parent, false)
        return UpcomingViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpcomingViewHolder, position: Int) {
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

        val isExpandable: Boolean = upcomingList[position].expandable
        holder.expendableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

        holder.linearLayout.setOnClickListener {
            val version = upcomingList[position]
            version.expandable = !item.expandable
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    //filter the items by the name
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                filteredList = if (charString.isEmpty()) {
                    upcomingList
                } else {
                    upcomingList.filter { item ->
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