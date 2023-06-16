package com.example.tasktune

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class CountdownTimerActivity : AppCompatActivity() {

    private lateinit var timeTxt: TextView
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var btnGiveUp: Button

    //private val countdownTime = 60  // 60 secunde, 1 min
    //private val clockTime = (countdownTime * 1000).toLong()
    //private val progressTime = (clockTime / 1000).toFloat()


    private var countdownTime: Int? = null
    private var clockTime: Long? = null
    private var progressTime: Float? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }

    }

    private lateinit var customCountdownTimer: CustomCountdownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown_timer)

        btnGiveUp = findViewById<Button>(R.id.btnGiveUp)

        val receivedTask = intent.getParcelableExtra<Task>("task")
        val format = SimpleDateFormat("HH:mm", Locale.US)
        val startDate = format.parse(receivedTask?.startHour)
        val endDate = format.parse(receivedTask?.endHour)
        val differenceInMillis = endDate.time - startDate.time

        this.countdownTime = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis).toInt()
        clockTime = (countdownTime!! * 1000).toLong()
        progressTime = (clockTime!! / 1000).toFloat()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        timeTxt = findViewById(R.id.timeTxt)
        circularProgressBar = findViewById(R.id.circularProgressBar)

        var secondsLeft = 0
        customCountdownTimer = object : CustomCountdownTimer(clockTime!!, 1000) {}
        customCountdownTimer.onTick = { millisUntilFinished ->
            var second = (millisUntilFinished / 1000.0f).roundToInt()
            if (second != secondsLeft) {
                secondsLeft = second

                timerFormat(
                    secondsLeft,
                    timeTxt
                )
            }
        }

        customCountdownTimer.onFinish = {
            timerFormat(
                0,
                timeTxt
            )
            Toast.makeText(this, "GATA", Toast.LENGTH_SHORT).show()
            showTaskDoneDialog()
        }

        circularProgressBar.max = progressTime!!.toInt()
        circularProgressBar.progress = progressTime!!.toInt()

        customCountdownTimer.startTimer()

        /**
        val pauseBtn = findViewById<Button>(R.id.pauseBtn)
        val resumeBtn = findViewById<Button>(R.id.resumebtn)
        val resetBtn = findViewById<Button>(R.id.resetBtn)

        pauseBtn.setOnClickListener {
            customCountdownTimer.pauseTimer()
        }

        resumeBtn.setOnClickListener {
            customCountdownTimer.resumeTimer()
        }

        resetBtn.setOnClickListener {
            circularProgressBar.progress = progressTime!!.toInt()
            customCountdownTimer.restartTimer()
        }
        */

        btnGiveUp.setOnClickListener {
            // Display the task done dialog
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task_give_up, null)
            val builder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)

            val dialog = builder.create()
            dialog.show()

            // Handle button click event
            val okButton = dialogView.findViewById<Button>(R.id.btnOk)
            okButton.setOnClickListener {
                // Start the new activity
                val intent = Intent(this, AppActivity::class.java)
                startActivity(intent)

                // Dismiss the dialog
                dialog.dismiss()
            }
        }
    }

    private fun showTaskDoneDialog() {
        // Display the task done dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task_done, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        // Handle button click event
        val okButton = dialogView.findViewById<Button>(R.id.btnOk)
        okButton.setOnClickListener {
            // Start the new activity
            val intent = Intent(this, AppActivity::class.java)
            startActivity(intent)

            // Dismiss the dialog
            dialog.dismiss()
        }
    }

    private fun timerFormat(secondsLeft: Int, timeTxt: TextView) {
        circularProgressBar.progress = secondsLeft

        val decimalFormat = DecimalFormat ("00")
        val hour = secondsLeft / 3600
        val min = (secondsLeft % 3600) / 60
        val seconds = secondsLeft % 60

        val timeFormat = "${decimalFormat.format(hour)}:${decimalFormat.format(min)}:${decimalFormat.format(seconds)}"

        timeTxt.text = timeFormat
    }

    private fun onBackPressedMethod() {
        customCountdownTimer.destroyTimer()
        finish()
    }

    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Confirm Exit")
            .setMessage("Are you sure you want to leave the app? Make sure to complete any pending actions.")
            .setPositiveButton("Exit") { _, _ ->
                super.onBackPressed() // Allow the app to exit
            }
            .setNegativeButton("Stay") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog and stay in the app
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }

    override fun onUserLeaveHint() {
        // Display a dialog box asking the user if they are sure they want to leave the app
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Confirm Exit")
            .setMessage("Are you sure you want to leave the app? Make sure to complete any pending actions.")
            .setPositiveButton("Exit") { _, _ ->
                super.onBackPressed() // Allow the app to exit
            }
            .setNegativeButton("Stay") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog and stay in the app
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }

    override fun onPause() {
        customCountdownTimer.pauseTimer()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        customCountdownTimer.resumeTimer()
    }

    override fun onDestroy() {
        customCountdownTimer.destroyTimer()
        super.onDestroy()
    }
}