package com.example.tasktune

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.tasktune.databinding.ActivityAppBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AppActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var binding: ActivityAppBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        val navView : NavigationView = findViewById(R.id.nav_view)

        auth = Firebase.auth
        drawerLayout = findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding = ActivityAppBinding.inflate(layoutInflater)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val headerView: View = navView.getHeaderView(0)
        val usernameView: TextView = headerView.findViewById(R.id.usernameView)

        val currentUserEmail = auth.currentUser?.email.toString()
        usernameView.text = currentUserEmail


        /**
         * Walk through app fragments
         */
        navView.setNavigationItemSelectedListener {
            it.isChecked = true

            when(it.itemId){
                R.id.nav_tasks -> replaceFragment(TasksFragment(), it.title.toString())
                //R.id.nav_friends -> replaceFragment(FriendsFragment(), it.title.toString())
                //R.id.nav_achievements -> replaceFragment(AchievementsFragment(), it.title.toString())
                //R.id.nav_statistics -> replaceFragment(StatisticsFragment(), it.title.toString())
                //R.id.nav_settings -> replaceFragment(SettingsFragment(), it.title.toString())
                R.id.nav_edit_profile -> replaceFragment(EditProfileFragment(), it.title.toString())
                //R.id.nav_logout -> Toast.makeText(applicationContext, "Clicked Logout", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    auth.signOut()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_help_feedback -> Toast.makeText(applicationContext, "Clicked Help and Feedback", Toast.LENGTH_SHORT).show()
            }

            true
        }

        // Set TasksFragment as the default fragment
        if (savedInstanceState == null) {
            replaceFragment(TasksFragment(), "Tasks")
            navView.setCheckedItem(R.id.nav_tasks)
        }
    }

    /**
     * Performs the fragments replacement
     */
    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
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