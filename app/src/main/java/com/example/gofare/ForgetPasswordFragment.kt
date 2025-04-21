package com.example.gofare

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forget_password, container, false)
    }

    private lateinit var backBtn : com.google.android.material.button.MaterialButton
    private lateinit var userEmail: EditText
    private lateinit var sendCodeBtn: TextView
    private lateinit var sendTimer: TextView

    private var countdown = 30
    private lateinit var countdownHandler: Handler
    private lateinit var countdownRunnable: Runnable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sendCodeBtn = view.findViewById(R.id.sendCodeBtn)
        sendTimer = view.findViewById(R.id.sendTimer)
        backBtn = view.findViewById(R.id.backBtn)
        userEmail = view.findViewById(R.id.usernameEditText)

        backBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                requireContext(),
                MainActivity::class.java
            )
            startActivity(intent)
        });

        sendCodeBtn.setOnClickListener {
            val email = userEmail.text.toString().trim()

            if (!email.contains("@")) {
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Reset email sent. Check your inbox.",
                            Toast.LENGTH_LONG
                        ).show()
                        startCooldown()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun startCooldown() {
        sendCodeBtn.isEnabled = false
        countdown = 30
        sendTimer.visibility = View.VISIBLE
        sendTimer.text = "Resend in ${countdown}s"

        countdownHandler = Handler(Looper.getMainLooper())
        countdownRunnable = object : Runnable {
            override fun run() {
                countdown--
                if (countdown > 0) {
                    sendTimer.text = "Resend in ${countdown}s"
                    countdownHandler.postDelayed(this, 1000)
                } else {
                    sendCodeBtn.isEnabled = true
                    sendTimer.text = ""
                    sendTimer.visibility = View.INVISIBLE
                }
            }
        }

        countdownHandler.postDelayed(countdownRunnable, 1000)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        if (::countdownHandler.isInitialized) {
            countdownHandler.removeCallbacks(countdownRunnable)
        }
    }


}