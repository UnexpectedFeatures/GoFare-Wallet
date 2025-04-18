package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import org.mindrot.jbcrypt.BCrypt
import com.example.gofare.databinding.ActivityPinBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PinActivity : AppCompatActivity() {

    lateinit var binding : ActivityPinBinding
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize ViewBinding and set it as content view
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.emailButton.setOnClickListener{
            auth.signOut()

            val intent = Intent(
                this@PinActivity,
                MainActivity::class.java
            )
            startActivity(intent)
        }

        val pinFields = listOf(
            binding.pin1, binding.pin2, binding.pin3,
            binding.pin4
        )

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

        pinFields[0].isFocusable = true
        pinFields[0].isFocusableInTouchMode = true

        for (btn in buttons) {
            assignId(btn, pinFields)
        }

        displayEmail()
    }

    private fun displayEmail() {
        auth = FirebaseAuth.getInstance() // Make sure this is initialized
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val viewModel = ViewModelProvider(this)[SharedViewModel::class.java]
            viewModel.observeUserData()

            // Assuming viewModel.email is a LiveData<String>
            viewModel.email.observe(this) { email ->
                binding.emailButton.text = email
            }
        } else {
            binding.emailButton.text = "No User"
        }
    }

    private fun clearPinFields(pinFields: List<TextView>) {
        pinFields.forEach {
            it.text = ""
            it.isFocusable = true
            it.isFocusableInTouchMode = true
        }
        pinFields[0].requestFocus()
    }

    private fun loginUser () {
        Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
        val inputPin = listOf(
            binding.pin1,
            binding.pin2,
            binding.pin3,
            binding.pin4
        ).joinToString("") { it.text.toString() }
        val userId = auth.currentUser?.uid

        if (userId != null){
            val pinRef = FirebaseFirestore.getInstance().collection("UserPin").document(userId)

            pinRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val hashedPin = document.getString("pin")
                        if (hashedPin != null) {
                            val isMatch = BCrypt.checkpw(inputPin, hashedPin)
                            if (isMatch) {
                                val intent = Intent(
                                    this@PinActivity,
                                    HomeActivity::class.java
                                )
                                startActivity(intent)
                            }
                            else{
                                Toast.makeText(this, "PIN Mismatch, Please try again", Toast.LENGTH_SHORT).show()
                                binding.pin1.text = ""
                                binding.pin2.text = ""
                                binding.pin3.text = ""
                                binding.pin4.text = ""
                            }

                        } else {
                            Log.d("UserPin", "PIN field not found.")
                        }
                    } else {
                        Log.d("UserPin", "Document does not exist.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("UserPin", "Failed to retrieve PIN", e)
                }

        }

    }

    private fun assignId(btn: MaterialButton, pinFields: List<TextView>) {
        btn.setOnClickListener {
            val digit = btn.text.toString()

            for (i in pinFields.indices) {
                val field = pinFields[i]

                if (field.text.isEmpty()) {
                    field.setText(digit)
                    field.requestFocus()

                    // Lock previous fields
                    for (j in 0 until i) {
                        pinFields[j].isFocusable = false
                        pinFields[j].isFocusableInTouchMode = false
                    }

                    break
                }
            }

            // ✅ Check if all are filled
            if (pinFields.all { it.text.isNotEmpty() }) {
                loginUser()
            }
        }
    }


}