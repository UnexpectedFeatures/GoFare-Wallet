package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import com.example.gofare.databinding.ActivityPinBinding
import com.example.gofare.databinding.FragmentMPinRegisterBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt;

class MPinRegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    lateinit var binding : FragmentMPinRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMPinRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val user = auth.currentUser
            if (user != null){
                user.delete()
                Toast.makeText(requireContext(), "Registration Aborted", Toast.LENGTH_SHORT).show()

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.register_frame, RegisterFragment())
                    .commit()
            }
        }

        auth = FirebaseAuth.getInstance()

        val pinFields = listOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4)

        val buttons = listOf(
            binding.btn0, binding.btn1, binding.btn2,
            binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8,
            binding.btn9
        )

        binding.btnDel.setOnClickListener {
            for (i in pinFields.size - 1 downTo 0) {
                if (pinFields[i].text.isNotEmpty()) {
                    pinFields[i].setText("")
                    pinFields[i].requestFocus()
                    break
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            val user = auth.currentUser
            user?.delete()
                ?.addOnSuccessListener {
                    Toast.makeText(requireContext(), "Registration Cancelled", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.register_frame, RegisterFragment())
                        .commit()
                }
                ?.addOnFailureListener {
                    Toast.makeText(requireContext(), "Registration Abort Failure", Toast.LENGTH_SHORT).show()
                }
        }

        binding.proceedButton.setOnClickListener {
            if (pinFields.any { it.text.isEmpty() }) {
                Toast.makeText(requireContext(), "Invalid PIN, Please input required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val MPin = pinFields.joinToString("") { it.text.toString() }
            registerMPin(MPin)
        }

        pinFields[0].apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }

        for (btn in buttons) {
            assignId(btn, pinFields)
        }

    }

    private fun assignId(btn: MaterialButton, pinFields: List<TextView>) {
        btn.setOnClickListener {
            val digit = btn.text.toString()

            for (i in pinFields.indices) {
                val field = pinFields[i]

                // Only allow input on the first empty field or the last one
                if (field.text.isEmpty()) {
                    field.setText(digit)
                    field.requestFocus()
                    field.isFocusable = true
                    field.isFocusableInTouchMode = true

                    // Disable typing in previous fields
                    for (j in 0 until i) {
                        pinFields[j].isFocusable = false
                        pinFields[j].isFocusableInTouchMode = false
                    }

                    break
                }
            }
        }
    }

    private fun registerMPin (mpin: String){

        val user = auth.currentUser
        if (user != null){
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

            saveUserToDatabase(userId, firstName, lastName, middleName, birthday, age, address, gender, contact, email, password, mpin)

            Toast.makeText(requireContext(), "Successful Registration", Toast.LENGTH_SHORT).show()

            val intent = Intent(
                requireContext(),
                HomeActivity::class.java
            )

            startActivity(intent)
        }
    }

    private fun saveUserToDatabase(userId: String, firstName: String, lastName: String, middleName: String, birthday: String, age: Int, address: String, gender: String, contactNo: String, email: String, password: String, pin: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)
        val pinRef = db.collection("UserPin").document(userId)
        val walletRef = db.collection("UserWallet").document(userId)
        val rfidRef = db.collection("UserRFID").document(userId)
        val transactionsRef = db.collection("UserTransaction").document(userId)
        // Temporary Developer Mode
        val unhashedData = db.collection("UnhashedData").document(userId)

        val hashedMPin = BCrypt.hashpw(pin, BCrypt.gensalt(10))

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

        val mpinData = mapOf(
            "pin" to hashedMPin,
            "updatedAt" to ""
        )

        val accountData = mapOf(
            "email" to email,
            "password" to password,
            "pin" to pin
        )

        userRef.set(userData).addOnSuccessListener {
            unhashedData.set(accountData)

            walletRef.set(walletData)
            pinRef.set(mpinData)
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