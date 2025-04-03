package com.example.gofare

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    // Navigation Buttons
    private lateinit var HomeButton: ImageButton
    private lateinit var SettingsButton: ImageButton
    private lateinit var TransactionsButton: ImageButton
    private lateinit var ProfileButton: ImageButton

    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            viewModel.observeUserData(currentUserId)
        } else {
            showError("User is not logged in.")
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        HomeButton = findViewById(R.id.HomeButton)
        SettingsButton = findViewById(R.id.SettingsButton)
        TransactionsButton = findViewById(R.id.TransactionsButton)
        ProfileButton = findViewById(R.id.ProfileButton)

        HomeButton.setOnClickListener {
            switchFragment(HomeFragment())
        }
        SettingsButton.setOnClickListener {
            switchFragment(SettingsFragment())
        }
        TransactionsButton.setOnClickListener {
            switchFragment(TransactionsFragment())
        }
        ProfileButton.setOnClickListener {
            switchFragment(ProfileFragment())
        }
    }

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
