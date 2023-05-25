package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        // set view binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_sign_up)
        setContentView(binding.root)

        supportActionBar?.hide()

        credentialsFocusListener()

        binding.btnSignUp.setOnClickListener() {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (credentialFinalChecker()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // account was created successfully
                        val databaseRef = database.reference.child("users").child(auth.currentUser!!.uid)
                        val usersRef = database.reference.child("users")
                        var numUsers: Long = 0

                        usersRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // Get the number of users
                                numUsers = dataSnapshot.childrenCount
                                // Do something with the number of users
                                // For example, log the value
                                Log.d("UserCount", "Number of users: $numUsers")
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle any errors that may occur
                                Log.e("FirebaseError", "Failed to read value: ${error.message}")
                            }
                        })

                        val numUsersPadd = "%0${10}d".format((numUsers+1).toInt())
                        val users : Users = Users(numUsersPadd, email, auth.currentUser!!.uid)

                        databaseRef.setValue(users).addOnCompleteListener {
                            if (it.isSuccessful) {
                                //auth.signOut()
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                var intent = Intent(this, SignInActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "A crapat aici", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }.addOnFailureListener { ex ->
                    // account was not created
                    if (ex is FirebaseAuthUserCollisionException){
                        // check if email address is already used
                        Toast.makeText(applicationContext, "Email address is already taken! Do you already have an account?", Toast.LENGTH_SHORT).show()
                    } else {
                        // display default error message
                        Log.e("Error", ex.toString())
                        Toast.makeText(applicationContext, "An error occurred while creating the account! Contact TaskTune client support team", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.signInOption.setOnClickListener {
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun writeEmailError() {
        binding.layoutInputEmail.error = checkEmail(binding.etEmail.text.toString())
    }

    private fun checkEmail(email: String): String? {
        if (email == "") {
            return "Enter an email address."
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
            return "Enter a password."
        }

        if (!password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")) ) {
            return "Password should have at least eight characters, at least one uppercase letter, one lowercase letter, one number and one special character"
        }

        return null
    }

    private fun writeConfirmPasswordError() {
        binding.layoutInputConfirmPassword.error = checkConfirmPassword(binding.etPassword.text.toString(), binding.etConfirmPassword.text.toString())
        binding.layoutInputConfirmPassword.errorIconDrawable = null
    }

    private fun checkConfirmPassword(password: String, confirmPassword: String): String? {
        if (confirmPassword == "") {
            return "Confirm the password entered above."
        }

        if (password != confirmPassword) {
            return "Passwords do not match."
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

        binding.etConfirmPassword.setOnFocusChangeListener {_, focused ->
            if (!focused) {
                writeConfirmPasswordError()
            }
        }
    }

    private fun credentialFinalChecker(): Boolean {
        writeEmailError()
        writePasswordError()
        writeConfirmPasswordError()

        val emailValidation = binding.layoutInputEmail.error == null
        val passwordValidation = binding.layoutInputPassword.error == null
        val confirmPasswordValidation = binding.layoutInputConfirmPassword.error == null

        if (emailValidation && passwordValidation && confirmPasswordValidation) {
            return true
        }
        return false
    }
}