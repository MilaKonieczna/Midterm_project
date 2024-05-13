package com.example.tryingmybest

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.example.tryingmybest.adapters.AdapterVaccines
import com.example.tryingmybest.data.DataVaccines
import com.example.tryingmybest.databinding.ActivityVaccinesBinding
import com.google.firebase.firestore.FirebaseFirestore

class Vaccines : AppCompatActivity() {

    private val vaccinesList = ArrayList<DataVaccines>()
    private lateinit var binding:ActivityVaccinesBinding
    private lateinit var adapter: AdapterVaccines
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =   ActivityVaccinesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AdapterVaccines(vaccinesList)
        binding.recyclerView.adapter = adapter

        val goUsers = findViewById<LinearLayout>(R.id.users)
        goUsers.setOnClickListener{goToUsers()}

        db.collection("vaccinations")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val dataVaccines = document.toObject(DataVaccines::class.java)
                        // Ensure you parse doses and duration to Int
                        vaccinesList.add(dataVaccines)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Error getting documents: ", exception)
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

    private fun goToUsers() {
        val intent = Intent(this, Users::class.java)
        startActivity(intent)
        finish()
    }
}
