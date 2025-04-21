package com.example.gofare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    lateinit var toRegister : TextView
    lateinit var toResetPassword : TextView
    lateinit var usernameInput : EditText
    lateinit var passwordInput : EditText
    lateinit var loginButton : MaterialButton
    lateinit var toggleVisibility : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        toRegister = findViewById(R.id.toRegister)
        usernameInput = findViewById(R.id.usernameEditText)
        passwordInput = findViewById(R.id.passwordEditText)
        toResetPassword = findViewById(R.id.forgotPassword)
        loginButton = findViewById(R.id.loginButton)
        toggleVisibility = findViewById(R.id.togglePasswordBtn)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            makeText(this, "Welcome Back ${currentUser.email}", LENGTH_LONG).show()
            val intent = Intent(
                this@MainActivity,
                PinActivity::class.java
            )
            startActivity(intent)
        }

        toRegister.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@MainActivity,
                RegisterActivity::class.java
            )
            startActivity(intent)
        })
        toResetPassword.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@MainActivity,
                ForgotPasswordActivity::class.java
            )
            startActivity(intent)
        })

        var isPasswordVisible = false

        toggleVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleVisibility.setImageResource(R.drawable.eye)
            } else {
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleVisibility.setImageResource(R.drawable.eye_closed)
            }

            passwordInput.setSelection(passwordInput.text.length) // Keep cursor at end
        }

        loginButton.setOnClickListener(View.OnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                makeText(this, "Please Fill Out The Fields", LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (!email.contains("@")) {
                makeText(this, "Please Enter a Valid Email", LENGTH_SHORT).show()
                return@OnClickListener
            }

            loginUser(email, password)
        })
    }

    fun loginUser(email: String, password: String) {
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    makeText(this, "Log In Success", LENGTH_SHORT).show()
                    Log.d("Firebase", "Login Successful")

                    Log.d("NFC Start", "NFC Check Starting")

                    if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
                        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                        }

                        if (tag != null) {
                            val tagId = tag.id.joinToString("") { "%02X".format(it) }
                            Log.d("NFC Tag", "Tag Detected: $tagId")

                            val userId = auth.currentUser?.uid
                            if (userId == null) {
                                Log.d("NFC Error", "User not authenticated")
                                return@addOnCompleteListener
                            }

                            val dynamicNfcRef = FirebaseFirestore.getInstance().collection("UserDynamicNFC").document(userId)

                            dynamicNfcRef.get().addOnSuccessListener { document ->
                                val previousId = document.getString("nfcId") ?: ""
                                Log.d("NFC Firestore", "Previous ID: $previousId")

                                if (previousId != tagId) {
                                    val nfcUpdate = mapOf(
                                        "nfcId" to tagId,
                                        "previousId" to previousId,
                                        "updatedAt" to Timestamp.now()
                                    )
                                    dynamicNfcRef.set(nfcUpdate)
                                        .addOnSuccessListener {
                                            Log.d("NFC Firestore", "NFC updated successfully")
                                        }
                                        .addOnFailureListener {
                                            Log.e("NFC Firestore", "Failed to update NFC", it)
                                        }
                                } else {
                                    Log.d("NFC Firestore", "Same tag as previous — no update needed")
                                }
                            }.addOnFailureListener {
                                Log.e("NFC Firestore", "Failed to read document", it)
                            }
                        } else {
                            Toast.makeText(this, "Device does not support NFC", Toast.LENGTH_SHORT).show()
                            Log.d("NFC Tag", "Tag is null")
                        }
                    }

                    val intent = Intent(
                        this@MainActivity,
                        PinActivity::class.java
                    )
                    startActivity(intent)
                } else {
                    Log.e("Firebase", "Login Failed", task.exception)
                    makeText(this, "Log In Failed", LENGTH_SHORT).show()
                }
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Transaction Notifications"
            val descriptionText = "Notifies when a new transaction is detected"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("transaction_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}