package com.example.tasktune

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChangeEmailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChangeEmailFragment : DialogFragment() {
    private var myView: View? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var password: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(context)
        myView = inflater.inflate(R.layout.fragment_change_email,null)

        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params

        builder.setView(myView)

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSave = myView?.findViewById<Button>(R.id.btnSave)
        val btnCancel = myView?.findViewById<Button>(R.id.btnCancel)
        val changedEmail = myView?.findViewById<EditText>(R.id.etEditEmailNew)
        password = myView?.findViewById<EditText>(R.id.etEditPass).toString()

        btnSave?.setOnClickListener {
            if (credentialFinalChecker()) {
                updateEmail(changedEmail?.text.toString())
            }
        }
        btnCancel?.setOnClickListener {
            dialog?.cancel()
            Toast.makeText(context, "No change has been made.", Toast.LENGTH_LONG).show()
        }
    }

    private fun writeEmailError() {
        myView?.findViewById<EditText>(R.id.etEditEmailNew)?.error = checkEmail(myView?.findViewById<EditText>(R.id.etEditEmailNew)?.text.toString())

    }

    private fun checkCurrentPassword(password: String): String? {
        if (password == "") {
            return "Enter your current password."
        }
        return null
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

    private fun credentialFinalChecker(): Boolean {
        writeEmailError()

        return myView?.findViewById<EditText>(R.id.etEditEmailNew)?.error == null
    }

    private fun updateEmail(newEmail: String) {
        val user = auth.currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Email address updated successfully.", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "Please Sign In to confirm your account.", Toast.LENGTH_SHORT).show()
                    startSignInActivity()
                } else {
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_LONG).show()
                    Log.e(tag, "Failed to update email address: ${task.exception?.message}")
                }
            }
        } else {
            Toast.makeText(context, "No user is currently Signed In.", Toast.LENGTH_SHORT).show()
            startSignInActivity()
        }
    }

    private fun startSignInActivity() {
        val intent = Intent(activity, SignInActivity::class.java)
        startActivity(intent)
    }
}