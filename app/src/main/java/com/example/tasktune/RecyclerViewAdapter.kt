package com.example.android_project

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktune.CountdownTimerActivity
import com.example.tasktune.DayTasks
import com.example.tasktune.R
import com.example.tasktune.Task
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat.startActivity
import java.io.Serializable


class RecyclerViewAdapter(private val context: Context, private val dayList: List<DayTasks>): RecyclerView.Adapter<RecyclerViewAdapter.DayViewHolder>() {

    private var buttonClickListener: OnButtonClickListener? = null
    var taskListFirebase: MutableList<Task> = mutableListOf()

    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        buttonClickListener = listener
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val eventsContainer: LinearLayout = itemView.findViewById(R.id.eventContainer)

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM - EEE", Locale.US)

        fun bind(dayData: DayTasks) {
            //Log.e("XXX ", button.text.toString())
            val date = inputFormat.parse(dayData.date)
            val formattedDate = outputFormat.format(date)

            dateTextView.text = formattedDate

            // Clear any previously added event views
            eventsContainer.removeAllViews()

            // Inflate and add CardViews dynamically
            val inflater = LayoutInflater.from(itemView.context)
            for (eventView in dayData.events) {
                var cardView: CardView
                if (eventView.tag == null) {
                    cardView = inflater.inflate(R.layout.recyclerview_no_task_model, eventsContainer, false) as CardView
                } else {
                    cardView = inflater.inflate(R.layout.recyclerview_model, eventsContainer, false) as CardView

                    // Customize the cardView with event data
                    val taskTitle = cardView.findViewById<TextView>(R.id.taskTitleView)
                    taskTitle.text = eventView.title

                    val taskTime = cardView.findViewById<TextView>(R.id.taskHourView)
                    taskTime.text = eventView.startHour + " - " + eventView.endHour

                    val taskTag = cardView.findViewById<TextView>(R.id.taskTagView)
                    taskTag.text = eventView.tag

                    //val button: Button = cardView.findViewById(R.id.btnStart)
                    //Log.e("XXX ", button.text.toString() + " " + button)
                    val button: Button = cardView.findViewById(R.id.btnStart)
                    button.setOnClickListener {
                        Log.e("XXX ", button.text.toString() + " " + button)
                        val intent = Intent(itemView.context, CountdownTimerActivity::class.java)
                        intent.putExtra("task", eventView)
                        itemView.context.startActivity(intent)
                    }
                }

                // Add the cardView to the eventsContainer
                eventsContainer.addView(cardView)
            }
        }
    }

    fun setFilteredList(taskListFirebase: MutableList<Task>){
        this.taskListFirebase = taskListFirebase
        notifyDataSetChanged()
    }

    interface OnButtonClickListener {
        fun onButtonClick(task: Task)
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
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