package com.example.gofare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

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

        toRegister = findViewById(R.id.toRegister);
        usernameInput = findViewById(R.id.usernameEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        toResetPassword = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.loginButton);
        toggleVisibility = findViewById(R.id.togglePasswordBtn);

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Welcome Back ${currentUser.email}", Toast.LENGTH_LONG).show()
            val intent = Intent(
                this@MainActivity,
                HomeActivity::class.java
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
//                PinActivity::class.java
            )
            startActivity(intent)
        });

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
                Toast.makeText(this, "Please Fill Out The Fields", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (!email.contains("@")) {
                Toast.makeText(this, "Please Enter a Valid Email", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            loginUser(email, password);
        })
    }

    fun loginUser(email: String, password: String) {
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Log In Success", Toast.LENGTH_SHORT).show()
                    Log.d("Firebase", "Login Successful")
                    val intent = Intent(
                        this@MainActivity,
                        HomeActivity::class.java
                    )
                    startActivity(intent)
                } else {
                    Log.e("Firebase", "Login Failed", task.getException())
                    Toast.makeText(this, "Log In Failed", Toast.LENGTH_SHORT).show()
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