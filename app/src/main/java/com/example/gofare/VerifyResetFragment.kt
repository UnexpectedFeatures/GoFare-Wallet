package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import com.google.firebase.auth.FirebaseAuth

class VerifyResetFragment : Fragment() {

    private var isVerified = false
    private lateinit var auth: FirebaseAuth
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify_reset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        requireActivity()

        val user = auth.currentUser
        user?.let {
            if (!it.isEmailVerified) {
                it.sendEmailVerification()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Verification email sent.", Toast.LENGTH_SHORT).show()
                        startVerificationLoop()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to send verification email: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                startVerificationLoop()
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

                        val mpinFragment = MPinRegisterFragment()
                        mpinFragment.arguments = arguments

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.register_frame, mpinFragment)
                            .commit()

                    } else {
                        handler.postDelayed(this, 3000) // try again in 3 seconds
                    }
                }
            }
        }

        handler.postDelayed(runnable, 3000) // initial delay
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::handler.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }
}