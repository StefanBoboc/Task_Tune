package com.example.tasktune

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChangePasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChangePasswordFragment : DialogFragment() {
    private var myView: View? = null
    private var attempts: Int = 3
    private lateinit var auth: FirebaseAuth
    private var currentPassword: TextInputEditText? = null
    private var newPassword: TextInputEditText? = null
    private var confirmPassword: TextInputEditText? = null
    private var layoutCurrentPassword: TextInputLayout? = null
    private var layoutNewPassword: TextInputLayout? = null
    private var layoutConfirmPassword: TextInputLayout? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(context)
        myView = inflater.inflate(R.layout.fragment_change_password,null)

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
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSave = myView?.findViewById<Button>(R.id.btnSave)
        val btnCancel = myView?.findViewById<Button>(R.id.btnCancel)
        currentPassword = myView?.findViewById(R.id.etEditPassCurrent)
        newPassword = myView?.findViewById(R.id.etEditPassNew)
        confirmPassword = myView?.findViewById(R.id.etEditPassConfirm)
        layoutCurrentPassword = myView?.findViewById(R.id.layoutInputEditPassCurrent)
        layoutNewPassword = myView?.findViewById(R.id.layoutInputEditPassNew)
        layoutConfirmPassword = myView?.findViewById(R.id.layoutInputEditPassConfirm)

        btnSave?.setOnClickListener {
            if (credentialFinalChecker()) {
                updatePassword(newPassword?.text.toString())
            }
        }

        btnCancel?.setOnClickListener {
            dialog?.cancel()
            Toast.makeText(context, "No change has been made.", Toast.LENGTH_LONG).show()
        }
    }

    private fun writeCurrentPasswordError() {
        layoutCurrentPassword?.error = checkCurrentPassword (
            password = currentPassword?.text.toString()
        )
        layoutCurrentPassword?.errorIconDrawable = null
    }

    private fun checkCurrentPassword(password: String): String? {
        if (password == "") {
            return "Enter your current password."
        }
        return null
    }

    private fun writeNewPasswordError() {
        layoutNewPassword?.error = checkNewPassword (
            password = newPassword?.text.toString()
        )
        layoutNewPassword?.errorIconDrawable = null
    }

    private fun checkNewPassword(password: String): String? {
        if (password == "") {
            return "Enter a new password."
        }

        if (!password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")) ) {
            return "Password should have at least eight characters, at least one uppercase letter, one lowercase letter, one number and one special character"
        }

        return null
    }

    private fun writeConfirmPasswordError() {
        layoutConfirmPassword?.error = checkConfirmPassword (
            password = newPassword?.text.toString(),
            confirmPassword = confirmPassword?.text.toString()
        )
        layoutConfirmPassword?.errorIconDrawable = null
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

    private fun credentialFinalChecker(): Boolean {
        writeCurrentPasswordError()
        writeNewPasswordError()
        writeConfirmPasswordError()

        val currentPasswordValidation = layoutCurrentPassword?.error == null
        val newPasswordValidation = layoutNewPassword?.error == null
        val confirmPasswordValidation = layoutConfirmPassword?.error == null

        return currentPasswordValidation && newPasswordValidation && confirmPasswordValidation
    }

    private fun updatePassword(newPassword: String) {
        val user = auth.currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword?.text.toString())
            user.reauthenticate(credential).addOnCompleteListener { reauthTask  ->
                if (reauthTask .isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener {updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                                Toast.makeText(context, "Please Sing In to confirm your account.", Toast.LENGTH_SHORT).show()
                                startSignInActivity()
                            } else {
                                Toast.makeText(context, "Failed to update password: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    attempts -= 1
                    if (attempts <= 0) {
                        Toast.makeText(context, "Invalid current password. Signing Out...", Toast.LENGTH_LONG).show()
                        startSignInActivity()
                    } else {
                        Toast.makeText(context, "Invalid current password. $attempts attempts left", Toast.LENGTH_LONG).show()
                    }
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