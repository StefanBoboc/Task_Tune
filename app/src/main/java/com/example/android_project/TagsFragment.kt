package com.example.android_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TagsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TagsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tags, container, false)

        val dayList = mutableListOf<DayData>()

        // Generate dummy day data
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -60)
        val dateFormat = SimpleDateFormat("dd MMM - EEE", Locale.ENGLISH)

        for (i in -30..100) {
            val date = dateFormat.format(calendar.time)
            val events = mutableListOf<DayData.EventDetails>()

            // Create CardView objects representing events
            val cardView1 = DayData.EventDetails("Titlu 1", "1", "10", "#tag1")
            // Customize cardView1 as needed
            events.add(cardView1)

            val cardView2 = DayData.EventDetails("Titlu 2", "2", "20", "#tag2")
            // Customize cardView2 as needed
            events.add(cardView2)

            dayList.add(DayData(date, events))

            calendar.add(Calendar.DAY_OF_MONTH, 1) // Move to the next day
        }

        // Set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecyclerViewAdapter(dayList)
        recyclerView.adapter = adapter

        // Scroll to the position of the current day
        val today = Calendar.getInstance()
        val position = adapter.getPositionForDate(today) // Assumes getPositionForDate() method in your adapter
        recyclerView.scrollToPosition(position)  // position = 60

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNewTask = view.findViewById<FloatingActionButton>(R.id.fab)

        btnNewTask.setOnClickListener {
            val showPopUp = PopUpCreateTaskFragment()
            showPopUp.show(parentFragmentManager, "showPopUp")
        }
    }
}