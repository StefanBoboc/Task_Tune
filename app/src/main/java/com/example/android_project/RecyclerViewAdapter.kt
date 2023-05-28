package com.example.android_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecyclerViewAdapter(private val dayList: List<DayData>): RecyclerView.Adapter<RecyclerViewAdapter.DayViewHolder>() {

    //private val item

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val eventsContainer: LinearLayout = itemView.findViewById(R.id.eventContainer)

        fun bind(dayData: DayData) {
            dateTextView.text = dayData.date

            // Clear any previously added event views
            eventsContainer.removeAllViews()

            // Inflate and add CardViews dynamically
            val inflater = LayoutInflater.from(itemView.context)
            for (eventView in dayData.events) {
                val cardView = inflater.inflate(R.layout.recyclerview_model, eventsContainer, false) as CardView
                // Customize the cardView with event data
                val taskTitle = cardView.findViewById<TextView>(R.id.taskTitle)
                taskTitle.text = eventView.taskTitle

                val taskTime = cardView.findViewById<TextView>(R.id.taskTime)
                taskTime.text = eventView.taskTimeStart + " - " + eventView.taskTimeEnd

                val taskTag = cardView.findViewById<TextView>(R.id.taskTag)
                taskTag.text = eventView.taskTag

                // Add the cardView to the eventsContainer
                eventsContainer.addView(cardView)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayData = dayList[position]
        holder.bind(dayData)
    }

    override fun getItemCount() = dayList.size

    fun getPositionForDate(date: Calendar): Int {
        val dateFormat = SimpleDateFormat("dd MMM - EEE", Locale.ENGLISH)
        val targetDate = dateFormat.format(date.time)

        for (i in 0 until dayList.size) {
            val dayData = dayList[i]
            if (dayData.date == targetDate) {
                return i
            }
        }

        return RecyclerView.NO_POSITION
    }
}