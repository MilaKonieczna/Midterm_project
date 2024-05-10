package com.example.tryingmybest

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tryingmybest.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * LogIn activity gives user access to the application. It verifies the user
 * with Firebase Authentication.
 */
class LogIn : AppCompatActivity() {

    /** The binding object that holds references to views within the activity's layout. */
    private lateinit var binding: ActivityLoginBinding

    /** The instance of FirebaseAuth for user authentication. */
    private lateinit var firebaseAuth: FirebaseAuth

    private var email: EditText? = null
    private var password: EditText? = null

    /**
     * Called when the activity is starting.
     * @param savedInstanceState The bundle containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating the layout and initializing the binding object
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initializing FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Setting up click listener for sign up text view
        val signUp: TextView = findViewById(R.id.SignUpTV)
        email = binding.emailEV
        password = binding.passwordEV

        signUp.setOnClickListener { goToSignUp() }

        // Setting up click listener for login button
        binding.button.setOnClickListener {
            if (validate()) register()
        }
    }

    /**
     * Validates user input for email and password fields.
     * @return true if email and password fields are not empty, false otherwise.
     */
    private fun validate(): Boolean {
        if (email?.text.toString().isEmpty() || password?.text.toString().isEmpty()) {
            Toast.makeText(this, "Please fill out all your information", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * Attempts to log in the user with the provided email and password using FirebaseAuth.
     * Shows toast messages based on the login result.
     */
    private fun register() {
        firebaseAuth.signInWithEmailAndPassword(email?.text.toString(), password?.text.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Login succeeded", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Changes activity to the SignUp activity.
     */
    private fun goToSignUp() {
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Changes activity to the Home activity.
     */
    private fun goToHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }
}
