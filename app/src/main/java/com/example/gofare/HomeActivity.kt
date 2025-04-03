package com.example.gofare

import android.os.Bundle
import android.view.View
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
    private lateinit var HomeButton : com.google.android.material.button.MaterialButton
    private lateinit var SettingsButton : com.google.android.material.button.MaterialButton
    private lateinit var TransactionsButton : com.google.android.material.button.MaterialButton
    private lateinit var ProfileButton : com.google.android.material.button.MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            fetchDataFromDatabase()
        }
        HomeButton = findViewById(R.id.HomeButton)
        SettingsButton = findViewById(R.id.SettingsButton)
        TransactionsButton = findViewById(R.id.TransactionsButton)
        ProfileButton = findViewById(R.id.ProfileButton)

        HomeButton.setOnClickListener(View.OnClickListener {
            switchFragment(HomeFragment())
        })
        SettingsButton.setOnClickListener(View.OnClickListener {
            switchFragment(SettingsFragment())
        })
        TransactionsButton.setOnClickListener(View.OnClickListener {
            switchFragment(TransactionsFragment())
        })
        ProfileButton.setOnClickListener(View.OnClickListener {
            switchFragment(ProfileFragment())
        })

    }

    fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun fetchDataFromDatabase() {
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        // Ensure user is logged in and UID is available
        if (currentUserId != null) {
            val database = FirebaseDatabase.getInstance().getReference("ClientReference").child(currentUserId)

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val client = dataSnapshot.getValue(Clients::class.java)

                    if (client != null) {
                        // Get ViewModel instanceLog.d("HomeActivity", "Client data loaded: ${client.firstName}")
                        //
                        val viewModel =
                            ViewModelProvider(this@HomeActivity)[SharedViewModel::class.java]

                        // Store data in ViewModel
                        viewModel.setUserData(
                            "${client.firstName} ${client.lastName}",
                            "${client.wallet?.currency}: ${client.wallet?.balance}"
                        )

                        // Load HomeFragment (no need to pass data via Bundle)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                    else{
                        showError("User is not logged in.")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    showError("Database error: ${databaseError.message}")
                }
            })
        } else {
            // If user is not logged in, show an error or redirect
            showError("User is not logged in.")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
