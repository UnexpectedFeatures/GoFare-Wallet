package com.example.gofare

import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome : TextView
    private lateinit var tvBalance : TextView
    private lateinit var userContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvWelcome = findViewById(R.id.tvWelcome);
        tvBalance = findViewById(R.id.tvBalance);

        // Initialize the LinearLayout container
        userContainer = findViewById(R.id.userContainer)

        // Get data from Firebase (or replace this with your actual database call)
        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        // Ensure user is logged in and UID is available
        if (currentUserId != null) {
            val database = FirebaseDatabase.getInstance().getReference("ClientReference")
                .child(currentUserId)

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Clear existing views
//                    userContainer.removeAllViews()

                    // Get the client object from the snapshot
                    val client = dataSnapshot.getValue(Clients::class.java)

                    // Add the client view dynamically
                    client?.let {
                        addUserView(it)
                    } ?: run {
                        // Handle case where data is null (no user data found)
                        showError("No client data available.")
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

    private fun addUserView(client: Clients) {
        // Create a new LinearLayout
        tvBalance.text = "${client.wallet?.balance.toString()}"
        tvWelcome.text = "Welcome ${client.firstName + " " + client.lastName}"
//        val userLayout = LinearLayout(this)
//        userLayout.orientation = LinearLayout.VERTICAL
//        userLayout.setPadding(8, 8, 8, 8)
//
//        // Create TextViews for each piece of information
//        val nameTv = TextView(this).apply {
//            text = "${client.firstName} ${client.lastName}"
//            textSize = 16f
//            setTypeface(null, Typeface.BOLD)
//        }
//
//        val emailTv = TextView(this).apply {
//            text = client.email
//            textSize = 14f
//        }
//
//        val balanceTv = TextView(this).apply {
//            text = "Balance: ${client.wallet?.balance} ${client.wallet?.currency}"
//            textSize = 14f
//        }
//
//        // Add the TextViews to the userLayout
//        userLayout.addView(nameTv)
//        userLayout.addView(emailTv)
//        userLayout.addView(balanceTv)

        // Add the userLayout to the userContainer
//        userContainer.addView(userLayout)
    }

    private fun showError(message: String) {
        // Display an error message using Toast
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
