package com.example.tryingmybest.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.tryingmybest.R
import com.example.tryingmybest.data.DataUser
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AdapterUsers(private val usersList: List<DataUser>) :
    RecyclerView.Adapter<AdapterUsers.UserViewHolder>(), Filterable {

    private var filteredList: List<DataUser> = usersList

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: EditText = itemView.findViewById(R.id.name)
        var delete: ShapeableImageView = itemView.findViewById(R.id.trash)
        var edit: ShapeableImageView = itemView.findViewById(R.id.edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item: DataUser = filteredList[position]
        holder.name.isEnabled = false
        holder.name.setText(item.username)

        // Set click listeners for delete and edit actions
        holder.delete.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, position)

        }

        holder.edit.setOnClickListener {
            val isEditMode = holder.name.isEnabled
            if (isEditMode) {
                val newName = holder.name.text.toString()
                val userToUpdate = filteredList[position].copy(username = newName)
                updateDataInFirestore(userToUpdate)
                holder.name.isEnabled = false
                holder.edit.setImageResource(R.drawable.edit_off)
            } else {
                holder.name.isEnabled = true
                holder.edit.setImageResource(R.drawable.edit)

            } }
    }


    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString().lowercase(Locale.getDefault())
                filteredList = if (charString.isEmpty()) {
                    usersList
                } else {
                    usersList.filter { user ->
                        user.username.lowercase(Locale.getDefault()).contains(charString)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                filteredList = filterResults?.values as? List<DataUser> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
    private fun updateDataInFirestore(dataUser: DataUser) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("email", dataUser.email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update(
                   "username", dataUser.username
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
    private fun showDeleteConfirmationDialog(context: Context, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the item if the user confirms
                deleteItem(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun deleteItem(position: Int) {
        val deletedUser = filteredList[position]

        // Remove the user from the filtered list
        filteredList = filteredList.toMutableList().apply {
            removeAt(position)
        }

        // Also remove the user from the original list
        val indexInOriginalList = usersList.indexOf(deletedUser)
        if (indexInOriginalList != -1) {
            usersList.toMutableList().removeAt(indexInOriginalList)
        }

        // Notify adapter about item removal
        notifyItemRemoved(position)

        // Delete the corresponding document from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("email", deletedUser.email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Document successfully deleted
                            println("DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            // Deletion failed
                            println("Error deleting document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                // Query failed
                println("Error querying document: $e")
            }
    }
}
