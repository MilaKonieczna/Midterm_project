package com.example.tryingmybest.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tryingmybest.R
import com.example.tryingmybest.data.DataVaccines
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

/**
 * Adapter for displaying vaccination history in a RecyclerView.
 * This adapter takes a list of [DataVaccines] objects and displays them in the RecyclerView.
 * It also provides filtering functionality to search for specific vaccine names.
 * @param vaccinesList The list of vaccination data to be displayed.
 */

@Suppress("UNCHECKED_CAST")
class AdapterVaccines(private val vaccinesList: List<DataVaccines>) :
    RecyclerView.Adapter<AdapterVaccines.VaccineViewHolder>(), Filterable {

    private var filteredList: List<DataVaccines> = vaccinesList

    inner class VaccineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var doses: EditText = itemView.findViewById(R.id.doses)
        var duration: EditText = itemView.findViewById(R.id.duration)
        var desc: EditText = itemView.findViewById(R.id.desc)
        var edit: ShapeableImageView = itemView.findViewById(R.id.edit)
        var save: Button = itemView.findViewById(R.id.save)
        var linearLayout: LinearLayout = itemView.findViewById(R.id.stable)
        var expendableLayout: RelativeLayout = itemView.findViewById(R.id.Expandable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_ddd, parent, false)
        return VaccineViewHolder(view)
    }

    override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
        val item: DataVaccines = filteredList[position]
        holder.name.text = item.name
        holder.doses.setText(item.doses.toString())
        holder.duration.setText(item.duration.toString())
        holder.desc.setText(item.desc)

        // Initially, set EditText fields to be non-editable
        holder.name.isEnabled = false
        holder.doses.isEnabled = false
        holder.duration.isEnabled = false
        holder.desc.isEnabled = false

        // Set OnClickListener on Shape ableImageView to enable editing
        holder.edit.setOnClickListener {
            holder.name.isEnabled = true
            holder.doses.isEnabled = true
            holder.duration.isEnabled = true
            holder.desc.isEnabled = true
        }

        // Set OnClickListener on Save button to save changes and disable editing
        holder.save.setOnClickListener {
            // Update DataVaccines object with new values
            val editedItem = filteredList[position]
            editedItem.name = holder.name.text.toString()
            editedItem.doses = holder.doses.text.toString().toInt()
            editedItem.duration = holder.duration.text.toString().toInt()
            editedItem.desc = holder.desc.text.toString()

            // Save changes to the database here
            // Assuming you have a function to update data in Firestore
            // Replace the line below with the actual code to update the database
            updateDataInFirestore(editedItem)

            // Disable editing after saving
            holder.name.isEnabled = false
            holder.doses.isEnabled = false
            holder.duration.isEnabled = false
            holder.desc.isEnabled = false
        }

        val isExpandable: Boolean = vaccinesList[position].expandable
        holder.expendableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

        holder.linearLayout.setOnClickListener {
            val version = vaccinesList[position]
            version.expandable = !item.expandable
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                filteredList = if (charString.isEmpty()) {
                    vaccinesList
                } else {
                    vaccinesList.filter { item ->
                        item.name.lowercase(Locale.getDefault()).contains(charString.lowercase(Locale.getDefault()))
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                filteredList = filterResults?.values as? List<DataVaccines> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

   private fun updateDataInFirestore(dataVaccines: DataVaccines) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("vaccinations")

        collectionRef.whereEqualTo("name", dataVaccines.name)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update(
                        "doses", dataVaccines.doses,
                        "duration", dataVaccines.duration,
                        "desc", dataVaccines.desc
                    )
                        .addOnSuccessListener {
                            // Update successful
                            println("DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            // Update failed
                            println("Error updating document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                // Query failed
                println("Error querying document: $e")
            }
    }

}
