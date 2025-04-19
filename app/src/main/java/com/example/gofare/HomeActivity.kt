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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null

    private lateinit var HomeButton: ImageButton
    private lateinit var SettingsButton: ImageButton
    private lateinit var TransactionsButton: ImageButton

    private lateinit var viewModel: SharedViewModel

    private var lastKnownTransactions: List<Transaction>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            viewModel.startLive()

            viewModel.notificationsEnabled.observe(this) { enabled ->
                if (enabled){
                    viewModel.transactions.observe(this) { list ->
                        if (lastKnownTransactions != null && list != lastKnownTransactions) {
                            NotificationHelper.sendNotification(
                                this,
                                "Transactions Updated",
                                "Your transactions have changed."
                            )
                        }
                        lastKnownTransactions = list
                    }
                }
            }

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

        HomeButton.setOnClickListener { switchFragment(HomeFragment()) }
        SettingsButton.setOnClickListener { switchFragment(SettingsFragment()) }
        TransactionsButton.setOnClickListener { switchFragment(TransactionsFragment()) }


        viewModel.rfid.observe(this) { rfid ->
            if (rfid.isNullOrEmpty()) {
                if (nfcAdapter == null){
                    TransactionsButton.visibility = View.GONE
                }
            } else {
                TransactionsButton.visibility = View.VISIBLE
            }
        }

        // Request notification permission if needed
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
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
