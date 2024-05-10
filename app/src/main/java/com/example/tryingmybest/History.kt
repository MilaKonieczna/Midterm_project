package com.example.tryingmybest

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.tryingmybest.adapters.AdapterHistory
import com.example.tryingmybest.data.DataVaxx
import com.example.tryingmybest.databinding.ActivityHistoryBinding
import com.google.android.material.imageview.ShapeableImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
/**
 * Activity for displaying vaccination history.
 * This activity retrieves vaccination data from Firestore based on the user's email,
 * filters out past vaccinations, and displays the remaining data in a RecyclerView.
 * Users can add new vaccinations and search for specific vaccines.
 */
class History : AppCompatActivity() {

    private val historyList = ArrayList<DataVaxx>()
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: AdapterHistory
    private val db = FirebaseFirestore.getInstance()
    private lateinit var format: SimpleDateFormat

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AdapterHistory(historyList)
        binding.recyclerView.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentTimestamp = System.currentTimeMillis() // Current timestamp
        db.collection("appointments")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val dataVaxx = document.toObject(DataVaxx::class.java)

                        if (dataVaxx.nextDose != null && dataVaxx.nextDose!!.time <= currentTimestamp) {
                            historyList.add(dataVaxx)
                            Log.d(ContentValues.TAG, "Document retrieved: $dataVaxx")
                        } else {
                            Log.d(ContentValues.TAG, "Document skipped due to past nextDose: $dataVaxx")
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e(ContentValues.TAG, "Error getting documents: ", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Error getting documents: ", exception)
            }

        val add = findViewById<ShapeableImageView>(R.id.add)
        add.setOnClickListener {
            val dialog = Add()
            dialog.show(supportFragmentManager, "AddDialogFragment")
        }

        val back = findViewById<ShapeableImageView>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        val editText =
            binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.off_white))
        editText.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText)
                return true
            }
        })
    }

    /**
     * Filters the vaccination history based on the search query.
     * @param query The search query entered by the user.
     */
    private fun filterItems(query: String?) {
        adapter.filter.filter(query)
    }
}
