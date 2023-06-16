package com.example.tasktune

import android.content.Intent
import androidx.appcompat.widget.SearchView
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
//import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.RecyclerViewAdapter
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TasksFragment : Fragment(), PopupDismissListener, RecyclerViewAdapter.OnButtonClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var view: View
    val taskListFirebase: MutableList<Task> = mutableListOf()
    private lateinit var adapter: RecyclerViewAdapter

    private var tasksList: MutableList<Task> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tasks, container, false)
        auth = Firebase.auth

        val tasksRef: DatabaseReference = database.reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("tasks")

        tasksList = mutableListOf()

        getTasksFromFirebase(tasksRef)

        return view
    }

    /**
     * Retrieve the saved tasks from the database and create Task
     * objects from them. The application displays the tasks from
     * the last 30 days and the next 90 days. If there are no saved
     * tasks for a specific day in the database, a blank task is created.
     *
     * Finally, it displays them using the RecyclerViewAdapter.
     */
    fun getTasksFromFirebase(tasksRef: DatabaseReference) {

        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // List with all the tasks ordered by days
                val dayTaskList: MutableList<DayTasks> = mutableListOf()
                //val taskListFirebase: MutableList<Task> = mutableListOf()

                // Displays the tasks from the last 30 days and the next 90 days.
                val currentDate = LocalDate.now()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val startDate = currentDate.minusDays(30)
                val endDate = currentDate.plusDays(90)

                var currentDateWalk = startDate

                while (currentDateWalk.isBefore(endDate)
                    || currentDateWalk.isEqual(endDate)
                ) {
                    val formattedDate = currentDateWalk.format(dateFormatter)

                    val taskList: MutableList<Task> = mutableListOf()

                    if (dataSnapshot.hasChild(formattedDate)) {
                        // There are saved tasks for the currentDateWalk
                        val dateSnapshot = dataSnapshot.child(formattedDate)

                        for (taskSnapshot in dateSnapshot.children) {
                            val title = taskSnapshot.child("title").getValue(String::class.java)
                            val date = taskSnapshot.child("date").getValue(String::class.java)
                            val startHour = taskSnapshot.child("startHour").getValue(String::class.java)
                            val endHour = taskSnapshot.child("endHour").getValue(String::class.java)
                            val tag = taskSnapshot.child("tag").getValue(String::class.java)

                            if (startHour != null && endHour != null && tag != null && title != null) {
                                val task = Task(title, date, startHour, endHour, tag)
                                taskList.add(task)
                                taskListFirebase.add(task)
                            }
                        }
                        sortTasksByStartHour(taskList)
                        dayTaskList.add(DayTasks(dateSnapshot.key.toString(), taskList))
                    } else {
                        // There are no tasks saved for the currentDateWalk. Create a "null" Task()
                        val task = Task("No tasks", null, null, null, null)
                        taskList.add(task)
                        dayTaskList.add(DayTasks(formattedDate, taskList))
                    }

                    // Go to the next currentDateWalk
                    currentDateWalk = currentDateWalk.plusDays(1)
                }

                val searchBar: SearchView = view.findViewById(R.id.layoutSearchBar)
                searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newTask: String?): Boolean {
                        if (newTask != null) {
                            filterList(newTask)
                        }
                        return true
                    }

                })


                // Set up the RecyclerView
                val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
                recyclerView.layoutManager = LinearLayoutManager(context)
                adapter = RecyclerViewAdapter(requireContext(), dayTaskList)
                recyclerView.adapter = adapter

                // Scroll to the position of the current day
                val today = Calendar.getInstance()
                val position = adapter.getPositionForDate(today) // Assumes getPositionForDate() method in your adapter
                recyclerView.scrollToPosition(position)  // position = 60


            }

            fun filterList(query: String) {
                println("FFFFFFFFFF")
                println(taskListFirebase)
                println(query)
                if (query != null) {
                    val filteredList = mutableListOf<Task>()
                    for (i in taskListFirebase) {
                        if (i.title?.lowercase(Locale.ROOT)?.contains(query) == true) {
                            filteredList.add(i)
                        }
                    }

                    if (filteredList.isEmpty()) {
                        Toast.makeText(context, "No Data found", Toast.LENGTH_SHORT).show()
                    } else {
                        adapter.setFilteredList(filteredList)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error, if necessary
            }
        })

        // Return the list after the asynchronous operation completes
//        return taskList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNewTask = view.findViewById<FloatingActionButton>(R.id.fab)

        btnNewTask.setOnClickListener {
            val showPopUp = CreateTaskFragment()
            showPopUp.setDismissListener(this)
            showPopUp.show(parentFragmentManager, "showPopUp")
        }

//        val adapter = RecyclerViewAdapter(dayTaskList)
//        adapter.setOnButtonClickListener(this)
//        recyclerView.adapter = adapter
    }

    override fun onButtonClick(task: Task) {
        // Start a new activity with the clicked task
        println("SSSSSSSSSSSS")
        //val intent = Intent(requireContext(), CountdownTimerActivity::class.java)
        //intent.putExtra("task", task)
        //startActivity(intent)
    }

    /**
     * The DialogFragment was dismissed
     */
    override fun onPopupDismissed() {
        refreshFragment()
    }

    /**
     * Refresh the first fragment when the pop-up is dismissed
     */
    private fun refreshFragment() {
        // Create a new instance of TasksFragment()
        val fragment = TasksFragment()

        // Replace the existing fragment with the new instance of the same fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TasksFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TasksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}