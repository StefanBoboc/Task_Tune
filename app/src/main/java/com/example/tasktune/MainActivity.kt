package com.example.tasktune

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_motion)
        supportActionBar?.hide()

        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        val currentUser = auth.currentUser

        Handler().postDelayed({
            if (currentUser != null) {
                // If theres already an account, go to home
                Log.e("Tag", "AAA Userul este cinectat ")

                val intent = Intent(this, AppActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // No user signed in... Go to Sing In
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 1500)
    }
}