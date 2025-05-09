package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterEmailFragment : Fragment() {

    private var isVerified = false
    private lateinit var auth: FirebaseAuth
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val user = auth.currentUser
            if (user != null){
                val storage = FirebaseStorage.getInstance().reference.child("studentIds/${user.uid}")
                storage.listAll()
                    .addOnSuccessListener { listResult ->
                        listResult.items.forEach { fileRef ->
                            fileRef.delete()
                                .addOnSuccessListener {
                                    Log.d("Delete", "Deleted: ${fileRef.name}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Delete", "Error deleting ${fileRef.name}", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Delete", "Error listing user files", e)
                    }
                user.delete()
                Toast.makeText(requireContext(), "Registration Aborted", Toast.LENGTH_SHORT).show()

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.register_frame, RegisterFragment())
                    .commit()
            }
        }

        auth = FirebaseAuth.getInstance()

        val email = arguments?.getString("email") ?: return
        val password = arguments?.getString("password") ?: return

        // Try logging in again (since user was signed out)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startVerificationLoop()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to re-login", Toast.LENGTH_SHORT).show()
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
