package com.example.tasktune

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.tasktune.databinding.ActivitySignInBinding
import com.example.tasktune.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        binding = ActivitySignInBinding.inflate(layoutInflater)

        supportActionBar?.hide()
        setContentView(binding.root)

        /**
         * Display errors whenever the entered information is not valid.
         */
        credentialsFocusListener()

        /**
         * Upon pressing the sign-in button, validate the entered information
         * with the ones in database. If all the information is valid, the user
         * is signed in.
         */
        binding.signInBtn.setOnClickListener() {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Check if all informations provided are valid
            if (credentialFinalChecker()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Sign In successfully
                        Toast.makeText(this, "Successfully sign in!", Toast.LENGTH_SHORT).show()

                        // Go to AppActivity
                        var intent = Intent(this, AppActivity::class.java)
                        startActivity(intent)

                        // destroy Activity
                        // finish()
                    }
                }.addOnFailureListener { ex ->
                    // Sign In failed
                    if (ex is FirebaseAuthInvalidCredentialsException || ex is FirebaseAuthInvalidUserException){
                        Toast.makeText(applicationContext, "Your TaskTune account was not found! Sign Up?", Toast.LENGTH_LONG).show()
                    } else {
                        // Display default error message
                        Log.e("Error", ex.toString())
                        Toast.makeText(applicationContext, "An error occurred while creating the account! Contact TaskTune client support team.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Provide valid information in each text field.", Toast.LENGTH_LONG).show()
            }
        }

        /**
         * For Sign Up operation go to SignUpActivity
         */
        binding.signUpOptionView.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun writeEmailError() {
        binding.emailInputLayout.error = checkEmail(binding.emailEditText.text.toString())
    }

    private fun writePasswordError() {
        binding.passwordInputLayout.error = checkPassword(binding.emailEditText.text.toString())
        binding.passwordInputLayout.errorIconDrawable = null
    }

    private fun checkEmail(email: String): String? {
        if (email.isEmpty()) {
            return "Enter your email address."
        }

        return null
    }

    private fun checkPassword(password: String): String? {
        if (password.isEmpty()) {
            return "Enter your password."
        }

        return null
    }

    /**
     * Display errors if the entered credentials are invalid. This verification
     * is performed when the focus is moved away from the object.
     */
    private fun credentialsFocusListener() {
        binding.emailEditText.setOnFocusChangeListener {_, focused ->
            if (!focused) {
                writeEmailError()
            }
        }

        binding.passwordEditText.setOnFocusChangeListener {_, focused ->
            if (!focused) {
                writePasswordError()
            }
        }
    }

    /**
     * Display errors if the entered credentials are invalid. This verification
     * occurs when the 'Sign In' button is pressed.
     */
    private fun credentialFinalChecker(): Boolean {
        writeEmailError()
        writePasswordError()

        val emailValidation = binding.emailInputLayout.error == null
        val passwordValidation = binding.passwordInputLayout.error == null

        if (emailValidation &&
            passwordValidation
        ) {
            return true
        }
        return false
    }
}