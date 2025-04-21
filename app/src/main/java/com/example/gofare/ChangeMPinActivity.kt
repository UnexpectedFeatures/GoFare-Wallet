package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gofare.databinding.ActivityChangeMpinBinding
import com.example.gofare.databinding.ActivityChangePinBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class ChangeMPinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityChangeMpinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeMpinBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    Toast.makeText(this, "Registration Cancelled", Toast.LENGTH_SHORT).show()
                    // Handle activity finish or return to another screen
                    finish() // Or redirect to another activity
                }
                ?.addOnFailureListener {
                    Toast.makeText(this, "Registration Abort Failure", Toast.LENGTH_SHORT).show()
                }
        }

        binding.proceedButton.setOnClickListener {
            if (pinFields.any { it.text.isEmpty() }) {
                Toast.makeText(
                    this,
                    "Invalid PIN, Please input required fields",
                    Toast.LENGTH_SHORT
                ).show()
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

        // Optional: Handle back press cancellation
        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(
                this@ChangeMPinActivity,
                MainActivity::class.java
            )
            startActivity(intent)
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
                    field.isFocusable = true
                    field.isFocusableInTouchMode = true

                    for (j in 0 until i) {
                        pinFields[j].isFocusable = false
                        pinFields[j].isFocusableInTouchMode = false
                    }
                    break
                }
            }
        }
    }

    private fun registerMPin(pin: String) {

        val userIn = auth.currentUser?.uid

        if (userIn != null) {
            val userPin = BCrypt.hashpw(pin,  BCrypt.gensalt(10))
            val pinRef = FirebaseFirestore.getInstance().collection("UserPin").document(userIn)

            val newPin = mapOf(
                "pin" to userPin,
                "updatedAt" to Timestamp.now()
            )

            pinRef.set(newPin).addOnSuccessListener {
                Toast.makeText(this, "Pin successfully changed", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    this@ChangeMPinActivity,
                    MainActivity::class.java
                )
                startActivity(intent)
            }.addOnFailureListener {
                Toast.makeText(this, "Pin change failure", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    this@ChangeMPinActivity,
                    MainActivity::class.java
                )
                startActivity(intent)
            }


        }

    }
}
