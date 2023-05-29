package com.example.android_project

import android.view.View
import androidx.cardview.widget.CardView

data class DayData(val date: String, val events: List<EventDetails>){
    @JvmSuppressWildcards
    data class EventDetails(
        val taskTitle: String? = null,
        val taskTimeStart: String? = null,
        val taskTimeEnd: String? = null,
        val taskTag: String? = null
    )
}



