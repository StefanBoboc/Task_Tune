//package com.example.android_project
//import com.google.firebase.database.*

import com.example.android_project.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object FirebaseManager {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun loadUserData(userId: String, callback: (Users?) -> Unit) {
        val userRef = database.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }
}
