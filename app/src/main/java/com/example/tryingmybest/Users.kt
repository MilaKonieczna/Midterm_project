package com.example.tryingmybest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tryingmybest.adapters.AdapterUsers
import com.example.tryingmybest.data.DataUser
import com.example.tryingmybest.databinding.ActivityUsersBinding
import com.google.firebase.firestore.FirebaseFirestore

class Users : AppCompatActivity() {

    private val usersList = ArrayList<DataUser>()
    private lateinit var binding: ActivityUsersBinding
    private lateinit var adapter: AdapterUsers
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()

        val goVaccines = findViewById<LinearLayout>(R.id.vaccines)
        goVaccines.setOnClickListener{goToVaccines()}


        fetchDataFromFirestore()
    }

    private fun setupRecyclerView() {
        adapter = AdapterUsers(usersList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearchView() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText)
                return true
            }
        })

        // Customize search view text color
        val editText = binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.off_white))
        editText.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchDataFromFirestore() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    usersList.add(document.toObject(DataUser::class.java))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { _ ->
            }
    }

    private fun filterItems(query: String?) {
        adapter.filter.filter(query)
    }

    private fun goToVaccines() {
        val intent = Intent(this, Vaccines::class.java)
        startActivity(intent)
        finish()
    }
}