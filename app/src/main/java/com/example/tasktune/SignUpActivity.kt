package com.example.tasktune

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktune.databinding.ActivitySignUpBinding
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
        //setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        supportActionBar?.hide()
        setContentView(binding.root)

        /**
         * Display errors whenever the entered information is not valid.
         */
        credentialsFocusListener()

        /**
         * Upon pressing the sign-up button, validate the entered information.
         * Also verify if the username is unique or not.
         * If all the information is valid, a new user is created in the database.
         */
        binding.signUpBtn.setOnClickListener() {
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Check if all informations provided are valid
            if (credentialFinalChecker()) {
                val usersRef = database.getReference("users")

                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Check if the username provided is valid or not
                        if (!checkIfUsernameExists(username, snapshot)) {
                            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    // Account was created successfully
                                    val databaseRefCurrUser = database.reference.child("users").child(auth.currentUser!!.uid)

                                    val userData = User(auth.currentUser!!.uid, username, email)

                                    // Save data in Realbase Firebase
                                    databaseRefCurrUser.setValue(userData).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            auth.signOut()

                                            Toast.makeText(this@SignUpActivity, "Account created successfully! Please Sign In", Toast.LENGTH_LONG).show()

                                            // Sign In too confirm your account
                                            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(this@SignUpActivity, "Failed to create account", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }.addOnFailureListener { ex ->
                                // Account was NOT created successfully
                                if (ex is FirebaseAuthUserCollisionException){
                                    Toast.makeText(applicationContext, "Email address is already taken! Do you already have an account?", Toast.LENGTH_LONG).show()
                                } else {
                                    // Display default error message
                                    Log.e("Error", ex.toString())
                                    Toast.makeText(applicationContext, "An error occurred! Contact TaskTune client support team.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(applicationContext, "Username is already taken!", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } else {
                Toast.makeText(applicationContext, "Provide valid information in each text field.", Toast.LENGTH_LONG).show()
            }
        }

        /**
         * For Sign In operation go to SignInActivity
         */
        binding.signInOptionView.setOnClickListener {
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

    }

    private fun writeUsernameError() {
        binding.usernameInputLayout.error = checkUsername(binding.usernameEditText.text.toString())
    }

    private fun writeEmailError() {
        binding.emailInputLayout.error = checkEmail(binding.emailEditText.text.toString())
    }

    private fun writePasswordError() {
        binding.passwordInputLayout.error = checkPassword(binding.passwordEditText.text.toString())
        binding.passwordInputLayout.errorIconDrawable = null
    }

    private fun writeConfirmPasswordError() {
        binding.confirmPasswordInputLayout.error = checkConfirmPassword(binding.passwordEditText.text.toString(), binding.confirmPasswordEditText.text.toString())
        binding.confirmPasswordInputLayout.errorIconDrawable = null
    }

    private fun checkUsername(username: String): String? {
        if (username.isEmpty()) {
            return "Enter an username."
        }

        if (!username.matches(Regex("^[A-Za-z\\d ]{1,20}$"))) {
            return "Username can't be longer than 20 characters and can't contain special characters."
        }

        return null
    }

    private fun checkEmail(email: String): String? {
        if (email.isEmpty()) {
            return "Enter an email address."
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email address."
        }

        return null
    }

    private fun checkPassword(password: String): String? {
        if (password.isEmpty()) {
            return "Enter a password."
        }

        if (!password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$"))) {
            return "Password should have at least 8 characters, at least one uppercase letter, one lowercase letter, one number and one special character."
        }

        return null
    }

    private fun checkConfirmPassword(password: String, confirmPassword: String): String? {
        if (confirmPassword.isEmpty()) {
            return "Confirm the password entered above."
        }

        if (password != confirmPassword) {
            return "Passwords do not match."
        }

        return null
    }

    /**
     * Display errors if the entered credentials are invalid. This verification
     * is performed when the focus is moved away from the object.
     */
    private fun credentialsFocusListener() {
        binding.usernameEditText.setOnFocusChangeListener {_, focused ->
            if (!focused) {
                writeUsernameError()
            }
        }

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

        binding.confirmPasswordEditText.setOnFocusChangeListener {_, focused ->
            if (!focused) {
                writeConfirmPasswordError()
            }
        }
    }

    /**
     * Display errors if the entered credentials are invalid. This verification
     * occurs when the 'Sign Up' button is pressed.
     */
    private fun credentialFinalChecker(): Boolean {
        writeUsernameError()
        writeEmailError()
        writePasswordError()
        writeConfirmPasswordError()

        val usernameValidation = binding.usernameInputLayout.error == null
        val emailValidation = binding.emailInputLayout.error == null
        val passwordValidation = binding.passwordInputLayout.error == null
        val confirmPasswordValidation = binding.confirmPasswordInputLayout.error == null

        if (usernameValidation &&
            emailValidation &&
            passwordValidation &&
            confirmPasswordValidation
        ) {
            return true
        }

        return false
    }

    /**
     * Check if the username provided as a parameter is unique or not.
     */
    private fun checkIfUsernameExists(username: String, datasnapshot: DataSnapshot): Boolean {
        Log.d("CheckIfUsernameExists", "checkIfUsernameExists: checking if $username already exists.")
        val user = User()
        for (ds in datasnapshot.children) {
            Log.d("CheckIfUsernameExists", "checkIfUsernameExists: datasnapshot: $ds")
            user.username = ds.getValue(User::class.java)!!.username
            Log.d("CheckIfUsernameExists", "checkIfUsernameExists: username: " + user.username)
            if (user.username.equals(username)) {
                Log.d("CheckIfUsernameExists", "checkIfUsernameExists: FOUND A MATCH: " + user.username)
                return true
            }
        }
        return false
    }
}