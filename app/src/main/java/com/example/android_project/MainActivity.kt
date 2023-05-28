package com.example.android_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Abc@abc.com
// Abc@abc1com

// Abc@abcd.com

class MainActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        auth = Firebase.auth

        // Load data from Firebase
        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if (user != null) {
                FirebaseManager.loadUserData(user.uid) { user ->
                    if (user != null) {
                        // Store the user data for future use in singleton)
                        UserDataManager.setUser(user)
                    } else {
                        // TBA: Handle error
                    }
                }

                // If theres already an account, go to home
                val intent = Intent(this, AppActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // No user signed in... Go to Sing In
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 0)

        //
        // MANAGE App Fragments
        //

//        drawerLayout = findViewById(R.id.drawerLayout)
//        val navView : NavigationView = findViewById(R.id.nav_view)
//
//        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        navView.setNavigationItemSelectedListener {
//            it.isChecked = true
//
//            when(it.itemId){
//                R.id.nav_tags -> replaceFragment(TagsFragment(), it.title.toString())
//                R.id.nav_friends -> replaceFragment(FriendsFragment(), it.title.toString())
//                R.id.nav_achievements -> replaceFragment(AchievementsFragment(), it.title.toString())
//                R.id.nav_statistics -> replaceFragment(StatisticsFragment(), it.title.toString())
//                R.id.nav_settings -> replaceFragment(SettingsFragment(), it.title.toString())
//                R.id.nav_edit_profile -> replaceFragment(EditProfileFragment(), it.title.toString())
////                R.id.nav_logout -> Toast.makeText(applicationContext, "Clicked Logout", Toast.LENGTH_SHORT).show()
//                R.id.nav_logout -> {
//                    auth.signOut()
//                    var intent = Intent(this, SignInActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }
//                R.id.nav_help_feedback -> Toast.makeText(applicationContext, "Clicked Help and Feedback", Toast.LENGTH_SHORT).show()
//            }
//
//            true
//        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        var fragmentManager = supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()

        drawerLayout.closeDrawers()

        setTitle(title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }
}