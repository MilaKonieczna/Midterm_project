package com.example.tryingmybest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tryingmybest.data.DataUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/**
 * SignUp activity allows users to create a new account. It verifies the user input
 * and saves user data to Firestore database when registration is successful.
 */
class SignUp : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private var email: EditText? = null
    private var username: EditText? = null
    private var password: EditText? = null
    private var repeat: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val register: Button = findViewById(R.id.save)

        auth = Firebase.auth

        email = findViewById(R.id.emailEV)
        username = findViewById(R.id.usernameEV)
        password = findViewById(R.id.passwordEV)
        repeat = findViewById(R.id.repeatEV)

        // Goes to log in activity if user has an account
        val logTv = findViewById<TextView>(R.id.LogTV)
        logTv.setOnClickListener{
            goToLogin()
        }

        // Setting up click listener for register button
        register.setOnClickListener {
            if (validate()) registerUser()

        }
    }

    /**
     * Validates user input for registration.
     * @return True if all input fields are valid, false otherwise.
     * Ex. Password needs to be no shorter than 6 characters,
     * have a capital letter, special sign and a number.
     */
    private fun validate(): Boolean {
        if (username?.text.isNullOrBlank()) {
            Toast.makeText(this, "Name is required",Toast.LENGTH_SHORT).show()
            return false
        }
        if (email?.text.isNullOrBlank()) {
            Toast.makeText(this, "Email is required",Toast.LENGTH_SHORT).show()
            return false
        }
        var monkey= false
        for (char in email?.text.toString()){
            if(char == '@'){
                monkey = true
                break
            }
        }
        if (!monkey){
            Toast.makeText(this, "Provide valid email!",Toast.LENGTH_SHORT).show()
            return false
        }
        if (password?.text.isNullOrBlank()) {
            Toast.makeText(this, "Password is required",Toast.LENGTH_SHORT).show()
            return false
        }
        if (password!!.length() < 6) {
            Toast.makeText(this, "Your password has to be at least 6 characters long!",Toast.LENGTH_SHORT).show()
            return false
        }
        var capital = false
        var special = false
        var number = false
        val specialCharacters = setOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '`',
            '~', '{', '}', ':', ';', '\"','\'', '<', '>', '.', '?', '/')
        val numbers = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

        for (char in password?.text.toString()) {
            if (numbers.contains(char)) {
                number = true
                break
            }
        }
        for (char in password?.text.toString()) {
            if (char.isUpperCase()) {
                capital = true
                break
            }
        }
        for (char in password?.text.toString()) {
            if (specialCharacters.contains(char)) {
                special = true
                break
            }
        }
        if (!capital) {
            Toast.makeText(this, "Your password should have a capital letter!",Toast.LENGTH_LONG).show()
            return false
        } else if (!special) {
            Toast.makeText(this, "Password should have a special sign!",Toast.LENGTH_LONG).show()
            return false
        } else if (!number) {
            Toast.makeText(this, "Password should have a number!",Toast.LENGTH_LONG).show()
            return false
        }

        if (!password?.text?.toString().equals(repeat?.text?.toString())) {
            Toast.makeText(this, "The passwords aren't the same!",Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    /**
     * Registers the user using FirebaseAuth and saves user data to Firestore upon successful registration.
     */
    private fun registerUser() {
        val userEmail = email?.text.toString().trim()
        val userPassword = password?.text.toString().trim()
        val userUsername = username?.text.toString().trim()

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = DataUser(userEmail, userUsername)
                    saveUserToFirestore(user)
                } else {
                    Toast.makeText(baseContext, "Sign up failed. Please try again.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Saves the user data to Firestore database.
     * @param user The User object containing user's email and username.
     */
    private fun saveUserToFirestore(user: DataUser) {
        // Save user data to Firestore
        firestore.collection("users")
            .add(user)
            .addOnSuccessListener { _ ->
                // Registration successful
                userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                // Registration failed
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Changes activity to the LogIn activity when the registration is successful.
     */
    private fun userRegistrationSuccess() {
        Toast.makeText(this, "Registration Success", Toast.LENGTH_LONG).show()
            goToLogin()
    }

    /**
     * Changes activity to the LogIn activity.
     */
    private fun goToLogin() {
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
        finish()
    }
}
