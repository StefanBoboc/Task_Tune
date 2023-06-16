package com.example.tasktune


import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CreateTaskFragment : DialogFragment() {
    private val auth: FirebaseAuth = Firebase.auth
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var databaseRef: DatabaseReference
    private lateinit var appContext: Context
    //private lateinit var databaseRef: DatabaseReference = database.reference.child("users").child(auth.currentUser!!.uid).child("tasks")

    private var view: View? = null
    private var title: EditText? = null
    private var date: TextView? = null
    private var startHour: TextView? = null
    private var endHour: TextView? = null
    private var tag: EditText? = null
    private var btnCancel: Button? = null
    private var btnSave: Button? = null

    private val months = listOf("Jan",	"Feb",	"Mar",	"Apr",	"May",	"Jun",	"Jul",	"Aug",	"Sep",	"Oct",	"Nov",	"Dec")

    private var dismissListener: PopupDismissListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(context)
        appContext = requireContext()
        view = inflater.inflate(R.layout.fragment_create_task, null)

        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params

        builder.setView(view)

        /** Get object references */
        title = view?.findViewById(R.id.taskTitleEditText)
        date = view?.findViewById(R.id.taskDateView)
        startHour = view?.findViewById(R.id.taskHourStartView)
        endHour = view?.findViewById(R.id.taskHourEndView)
        tag = view?.findViewById(R.id.taskTagEditText)
        btnCancel = view?.findViewById<Button>(R.id.btnCancel)
        btnSave = view?.findViewById<Button>(R.id.btnSave)

        /** Set default Day and Hours for the task */
        setDefaultValues()

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_task, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        date?.setOnClickListener {
            val dpd = DatePickerDialog (requireContext(), DatePickerDialog.OnDateSetListener {
                    _, mYear, mMonth, mDay ->
                date?.text =  String.format("%d %s %d", mDay, months[mMonth], mYear)
            }, year, month, day)

            dpd.show()
        }

        startHour?.setOnClickListener {
            val tpd = TimePickerDialog (requireContext(), TimePickerDialog.OnTimeSetListener {
                    _, mHour, mMinute ->
                val selectedTime = String.format("%02d:%02d", mHour, mMinute)
                startHour?.text = selectedTime
            }, hour, minute,true)

            tpd.show()
        }

        endHour?.setOnClickListener {
            val tpd = TimePickerDialog (requireContext(), TimePickerDialog.OnTimeSetListener {
                    _, mHour, mMinute ->
                val selectedTime = String.format("%02d:%02d", mHour, mMinute)
                endHour?.text = selectedTime
            }, hour, minute,true)

            tpd.show()
        }

        btnCancel?.setOnClickListener {
            dialog?.cancel()
            Toast.makeText(context, "No change has been made.", Toast.LENGTH_LONG).show()
        }

        btnSave?.setOnClickListener {
            if (credentialFinalChecker()) {

                // Format the date in a new one
                val inputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val dateVal = LocalDate.parse(date?.text.toString(), inputFormatter)
                val formattedDate = dateVal.format(outputFormatter)

                val task = Task(
                    title?.text.toString(),
                    formattedDate,
                    startHour?.text.toString(),
                    endHour?.text.toString(),
                    tag?.text.toString()
                )

                val dayRef = database.reference
                    .child("users")
                    .child(auth.currentUser!!.uid)
                    .child("tasks")
                    .child(formattedDate)
                val taskId = dayRef.push().key

                if (taskId != null) {
                    dayRef.child(taskId).setValue(task)
                        .addOnSuccessListener {
                            // Task saved successfully
                            Toast.makeText(appContext, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                            Log.i("btnSave.setOnClickListener", "Task saved successfully")
                        }
                        .addOnFailureListener {
                            // Handle any errors
                            Toast.makeText(appContext, "Task not saved.", Toast.LENGTH_SHORT).show()
                            Log.e("btnSave.setOnClickListener", "Task not saved")
                        }
                }

                dismissListener?.onPopupDismissed()
                dismiss()
            } else {
                Toast.makeText(appContext, "Provide valid information in each text field.", Toast.LENGTH_LONG).show()
                Log.e("btnSave.setOnClickListener", "Provide valid information in each text field")
            }
        }
    }

    private fun checkTaskTitle(taskTitleVal: String) {
        if (taskTitleVal.isEmpty()) {
            title?.error = "Enter a task title."
            return
        } else if (taskTitleVal.length > 20) {
            title?.error = "Title can't be longer than 20 characters."
            return
        } else {
            title?.error = null
        }
    }

    private fun checkTaskTag(taskTagVal: String) {
        if (taskTagVal.isEmpty()) {
            tag?.error = ("Enter a task tag")
        } else if (taskTagVal.length > 20) {
            tag?.error = "Title can't be longer than 20 characters."
            return
        } else {
            tag?.error = null
        }
    }

    private fun credentialFinalChecker(): Boolean {
        checkTaskTitle(title?.text.toString())
        checkTaskTag(tag?.text.toString())

        val taskTitleValidation = title?.error == null
        val taskTagValidation = tag?.error == null

        return taskTitleValidation && taskTagValidation
    }

    /**
     * Set default Day and Hours for the task
     * with the current date and hour
     */
    private fun setDefaultValues() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val selectedDate = String.format("%d %s %d", day, months[month], year)
        val selectedTime = String.format("%02d:%02d", hour, minute)

        date?.text = selectedDate
        startHour?.text = selectedTime
        endHour?.text = selectedTime
    }

    fun setDismissListener(listener: PopupDismissListener) {
        dismissListener = listener
    }
}