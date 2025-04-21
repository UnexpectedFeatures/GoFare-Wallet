package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class VerifyMPinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_mpin)

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@VerifyMPinActivity, MainActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            if (!it.isEmailVerified) {
                it.sendEmailVerification()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
                        startVerificationLoop()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to send verification email: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                startVerificationLoop() // Already verified
            }
        }
    }

    private fun startVerificationLoop() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val user = auth.currentUser
                user?.reload()?.addOnSuccessListener {
                    if (user.isEmailVerified && !isVerified) {
                        isVerified = true
                        Toast.makeText(this@VerifyMPinActivity, "Email Verified!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@VerifyMPinActivity, ChangeMPinActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        handler.postDelayed(this, 3000)
                    }
                }
            }
        }

        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::handler.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }
}
