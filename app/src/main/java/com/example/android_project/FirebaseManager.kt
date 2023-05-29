//package com.example.android_project
//import com.google.firebase.database.*

import com.example.android_project.DayData
import com.example.android_project.Task
import com.example.android_project.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.children
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun loadUserData(userId: String, callback: (Users?) -> Unit) {
        val userRef = database.child("users/${userId}/")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userAux = dataSnapshot.getValue(Users::class.java)
                val uid: String? = userAux?.uid
                val username: String? = userAux?.username
                val email: String? = userAux?.email
                val photoUrl: String? = userAux?.photoUrl
                val taskList = mutableListOf<Task>()

                for (userSnapshot in dataSnapshot.children) {
//                    println(userSnapshot.)
                    for (taskSnapshot in userSnapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        println("AAA $task")
                        taskList.add(task!!)
                    }
                }

                val user = Users(userAux?.uid, userAux?.username, userAux?.email, userAux?.photoUrl)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error here if retrieval is unsuccessful
                println("Error: ${databaseError.message}")
            }
        })


    }
}

//        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val user = snapshot.getValue(Users::class.java)
//
//
//                callback(user)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                callback(null)
//            }
//        })

// Use the taskList here, containing the retrieved tasks data
//                for (task in taskList) {
//                    println("Task Name: ${task.taskTitle.toString()}")
//                    println("Task Name: ${task.taskDate}")
//                    println("Start Time: ${task.taskTimeStart}")
//                    println("End Time: ${task.taskTimeEnd}")
//                    println("Tag: ${task.taskTag}")
//                    println("-----------------------------")
//                }
