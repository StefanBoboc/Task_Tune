package com.example.android_project

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Paint
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
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*

class PopUpCreateTaskFragment : DialogFragment() {
    private var auth: FirebaseAuth = Firebase.auth
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseRef: DatabaseReference = database.reference.child("users").child(auth.currentUser!!.uid).child("tasks")

    private var myView: View? = null
    private var taskTitle: EditText? = null
    private var taskDate: TextView? = null
    private var taskTimeStart: TextView? = null
    private var taskTimeEnd: TextView? = null
    private var taskTag: EditText? = null

    private var months = listOf("Jan",	"Feb",	"Mar",	"Apr",	"May",	"June",	"July",	"Aug",	"Sept",	"Oct",	"Nov",	"Dec")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(context)
        myView = inflater.inflate(R.layout.fragment_pop_up_create_task,null)

        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params

        builder.setView(myView)

        taskTitle = myView?.findViewById<EditText>(R.id.taskTitle)
        taskDate = myView?.findViewById<TextView>(R.id.taskDate)
        taskTimeStart = myView?.findViewById<TextView>(R.id.taskTimeStart)
        taskTimeEnd = myView?.findViewById<TextView>(R.id.taskTimeEnd)
        taskTag = myView?.findViewById<EditText>(R.id.taskTag)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val selectedDate = "" + day + " " + months[month] + " " + year + ""
        val selectedTime = String.format("%02d:%02d", hour, minute)

        taskDate?.text = selectedDate
        taskTimeStart?.text = selectedTime
        taskTimeEnd?.text = selectedTime

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pop_up_edit_pass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSave = myView?.findViewById<Button>(R.id.btnSave)
        val btnCancel = myView?.findViewById<Button>(R.id.btnCancel)

        btnSave?.setOnClickListener {
            if (credentialFinalChecker()) {
                val taskKey: String? = databaseRef.push().key
                if (taskKey != null) {
                    val task: DayData.EventDetails = DayData.EventDetails(
                        taskTitle?.text.toString(),
                        taskTimeStart?.text.toString(),
                        taskTimeEnd?.text.toString(),
                        taskTag?.text.toString()
                    )
                    databaseRef.child(taskDate?.text.toString()).child(taskKey).setValue(task).addOnCompleteListener {
                        if (it.isSuccessful) {
                            // add new task in the day
                            Toast.makeText(context, "Task created successfully!", Toast.LENGTH_SHORT).show()
                            dialog?.cancel()
                        } else {
                            Toast.makeText(context, "Failed to create task", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnCancel?.setOnClickListener {
            dialog?.cancel()
            Toast.makeText(context, "No change has been made.", Toast.LENGTH_LONG).show()
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        taskDate?.setOnClickListener {
            val dpd = DatePickerDialog (requireContext(), DatePickerDialog.OnDateSetListener {
                    _, mYear, mMonth, mDay ->
                taskDate?.text = "" + mDay + " " + months[mMonth] + " " + mYear + ""
            }, year, month, day)

            dpd.show()
        }

        taskTimeStart?.setOnClickListener {
            val tpd = TimePickerDialog (requireContext(), TimePickerDialog.OnTimeSetListener {
                    _, mHour, mMinute ->
                val selectedTime = String.format("%02d:%02d", mHour, mMinute)
                // Update your UI with the selected time
                taskTimeStart?.text = selectedTime
            }, hour, minute,true)

            tpd.show()
        }

        taskTimeEnd?.setOnClickListener {
            val tpd = TimePickerDialog (requireContext(), TimePickerDialog.OnTimeSetListener {
                    _, mHour, mMinute ->
                val selectedTime = String.format("%02d:%02d", mHour, mMinute)
                // Update your UI with the selected time
                taskTimeEnd?.text = selectedTime
            }, hour, minute,true)

            tpd.show()
        }
    }

    private fun checkTaskTitle(taskTitleVal: String) {
        if (taskTitleVal.isEmpty()) {
            taskTitle?.error = "Enter a task title"
            return
        } else if (taskTitleVal.contains(";")) {
            taskTitle?.error = "Illegal character ';'"
            return
        } else {
            taskTitle?.error = null
        }
    }

    private fun checkTaskTag(taskTagVal: String) {
        if (taskTagVal.isEmpty()) {
            taskTag?.error = ("Enter a task tag")
        } else if (taskTagVal.contains(";")) {
            taskTag?.error = "Illegal character ';'"
            return
        } else {
            taskTag?.error = null
        }
    }

    private fun credentialFinalChecker(): Boolean {
        checkTaskTitle(taskTitle?.text.toString())
        checkTaskTag(taskTag?.text.toString())

        val taskTitleValidation = taskTitle?.error == null
        val taskTagValidation = taskTag?.error == null

        return taskTitleValidation && taskTagValidation
    }
}