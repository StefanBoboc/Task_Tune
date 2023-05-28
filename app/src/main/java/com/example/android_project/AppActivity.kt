package com.example.android_project

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AppActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        auth = Firebase.auth

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Walk through app fragments
        navView.setNavigationItemSelectedListener {
            it.isChecked = true

            when(it.itemId){
                R.id.nav_tags -> replaceFragment(TagsFragment(), it.title.toString())
                R.id.nav_friends -> replaceFragment(FriendsFragment(), it.title.toString())
                R.id.nav_achievements -> replaceFragment(AchievementsFragment(), it.title.toString())
                R.id.nav_statistics -> replaceFragment(StatisticsFragment(), it.title.toString())
                R.id.nav_settings -> replaceFragment(SettingsFragment(), it.title.toString())
                R.id.nav_edit_profile -> replaceFragment(EditProfileFragment(), it.title.toString())
//                R.id.nav_logout -> Toast.makeText(applicationContext, "Clicked Logout", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    auth.signOut()
                    var intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_help_feedback -> Toast.makeText(applicationContext, "Clicked Help and Feedback", Toast.LENGTH_SHORT).show()
            }

            true
        }

        // Set TagsFragment as the default fragment
        if (savedInstanceState == null) {
            replaceFragment(TagsFragment(), "Tags")
            navView.setCheckedItem(R.id.nav_tags)
        }
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