package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var toRegister : TextView
    lateinit var toResetPassword : TextView
    lateinit var emailInput : EditText
    lateinit var passwordInput : EditText
    lateinit var loginButton : MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        toRegister = findViewById(R.id.toRegister);
        emailInput = findViewById(R.id.emailEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        toResetPassword = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.loginButton);

        toRegister.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@MainActivity,
                RegisterActivity::class.java
            )
            startActivity(intent)
        });

        loginButton.setOnClickListener(View.OnClickListener {
            val email = emailInput.text.toString().trim()
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
                }
            }
    }

}