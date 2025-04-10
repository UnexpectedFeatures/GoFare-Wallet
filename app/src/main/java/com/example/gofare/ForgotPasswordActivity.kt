package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var confirmBtn : com.google.android.material.button.MaterialButton
    private lateinit var backBtn : com.google.android.material.button.MaterialButton
    private lateinit var userEmail: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var togglePasswordBtn: ImageButton
    private lateinit var toggleConfirmPasswordBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        confirmBtn = findViewById(R.id.confirmButton)
        backBtn = findViewById(R.id.confirmButton)
        userEmail = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        togglePasswordBtn = findViewById(R.id.togglePasswordBtn)
        toggleConfirmPasswordBtn = findViewById(R.id.toggleConfirmPasswordBtn)

        backBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@ForgotPasswordActivity,
                MainActivity::class.java
            )
            startActivity(intent)
        });

        var isPasswordVisible = false
        var isConfirmPasswordVisible = false

        togglePasswordBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.eye)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.eye_closed)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        toggleConfirmPasswordBtn.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible

            if (isConfirmPasswordVisible) {
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.eye)
            } else {
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.eye_closed)
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        confirmBtn.setOnClickListener{
            updateProfile()
        }

    }

    private fun updateProfile() {
        val email = userEmail.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (confirmPassword != password || confirmPassword.isEmpty() || password.isEmpty()) return
        if (!email.contains("@")) return


    }


}