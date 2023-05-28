package com.example.android_project

import android.view.View
import androidx.cardview.widget.CardView

data class DayData(val date: String, val events: List<EventDetails>){
    data class EventDetails(
        val taskTitle: String,
        val taskTimeStart: String,
        val taskTimeEnd: String,
        val taskTag: String
    )
}

