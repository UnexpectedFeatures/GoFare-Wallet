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
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
                        val userId = user.uid

                        val firstName = arguments?.getString("firstName") ?: ""
                        val lastName = arguments?.getString("lastName") ?: ""
                        val middleName = arguments?.getString("middleName") ?: ""
                        val birthday = arguments?.getString("birthday") ?: ""
                        val age = arguments?.getInt("age") ?: 0
                        val address = arguments?.getString("address") ?: ""
                        val gender = arguments?.getString("gender") ?: ""
                        val contact = arguments?.getString("contact") ?: ""
                        val email = arguments?.getString("email") ?: ""
                        val password = arguments?.getString("password") ?: ""

                        saveUserToDatabase(userId, firstName, lastName, middleName, birthday, age, address, gender, contact, email, password)

                        val intent = Intent(
                            requireContext(),
                            HomeActivity::class.java
                        )

                        startActivity(intent)
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

    private fun saveUserToDatabase(userId: String, firstName: String, lastName: String, middleName: String, birthday: String, age: Int, address: String, gender: String, contactNo: String, email: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)
        val walletRef = db.collection("UserWallet").document(userId)
        val rfidRef = db.collection("UserRFID").document(userId)
        val transactionsRef = db.collection("UserTransaction").document(userId)

        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "middleName" to middleName,
            "age" to age,
            "birthday" to birthday,
            "address" to address,
            "gender" to gender,
            "contactNumber" to contactNo,
            "email" to email,
            "creationDate" to Timestamp.now(),
            "updateDate" to Timestamp.now(),
            "enabled" to true
        )

        val walletData = mapOf(
            "balance" to 0,
            "currency" to "PHP",
            "loaned" to false,
            "loanedAmount" to 0,
        )

        val rfidData = mapOf(
            "registeredAt" to "",
            "renewedAt" to "",
            "rfid" to "",
        )

        userRef.set(userData).addOnSuccessListener {
            walletRef.set(walletData)
            rfidRef.set(rfidData)
            transactionsRef.set(emptyMap<String, Any>())

            Toast.makeText(requireContext(), "Registration Complete!", Toast.LENGTH_SHORT).show()
            Log.d("Register", "User Registration Successful")
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Register", "User Registration Failed", e)
        }
    }
}
