package com.example.android_project

data class Users(
    var uid: String? = null,
    var username: String? = null,
    var email: String? = null,
    var photoUrl: String? = null,
    var tasks:  HashMap<String,Task>? = null
)
