package com.example.android_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.android_project.databinding.ActivitySignInBinding
import com.example.android_project.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        // set view binding
        binding = ActivitySignInBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_sign_in)
        setContentView(binding.root)

        supportActionBar?.hide()

        credentialsFocusListener()

        binding.btnSignUp.setOnClickListener() {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (checkAllFields()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // sign in successful
                        Toast.makeText(this, "Successfully Sign In", Toast.LENGTH_SHORT).show()

                        // go to AppActivity
                        var intent = Intent(this, AppActivity::class.java)
                        startActivity(intent)

                        // destroy Activity
                        // finish()
                    }
                }.addOnFailureListener { ex ->
                    // sign in failed
                    if (ex is FirebaseAuthInvalidCredentialsException || ex is FirebaseAuthInvalidUserException){
                        // check if the account exists
                        Toast.makeText(applicationContext, "Your TaskTune account was not found! Sign Up?", Toast.LENGTH_SHORT).show()
                    } else {
                        // display default error message
                        Log.e("Error", ex.toString())
                        Toast.makeText(applicationContext, "An error occurred while creating the account! Contact TaskTune client support team.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.signUpOption.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun writeEmailError() {
        binding.layoutInputEmail.error = checkEmail(binding.etEmail.text.toString())
    }

    private fun checkEmail(email: String): String? {
        if (email == "") {
            return "Enter your email address."
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Enter a valid email address and try again."
        }

        return null
    }

    private fun writePasswordError() {
        binding.layoutInputPassword.error = checkPassword(binding.etPassword.text.toString())
        binding.layoutInputPassword.errorIconDrawable = null
    }

    private fun checkPassword(password: String): String? {
        if (password == "") {
            return "Enter your password."
        }

        return null
    }

    private fun credentialsFocusListener() {
        binding.etEmail.setOnFocusChangeListener {_, focused ->
            if (!focused) {
                writeEmailError()
            }
        }

        binding.etPassword.setOnFocusChangeListener {_, focused ->
            if (!focused) {
                writePasswordError()
            }
        }
    }

    private fun checkAllFields(): Boolean {
        writeEmailError()
        writePasswordError()

        val emailValidation = binding.layoutInputEmail.error == null
        val passwordValidation = binding.layoutInputPassword.error == null

        if (emailValidation && passwordValidation) {
            return true
        }
        return false
    }
}