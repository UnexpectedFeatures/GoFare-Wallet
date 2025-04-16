package com.example.gofare

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gofare.databinding.ActivityPinBinding
import com.google.android.material.button.MaterialButton

class PinActivity : AppCompatActivity() {

    lateinit var binding : ActivityPinBinding
    lateinit var pin1 : TextView
    lateinit var pin2 : TextView
    lateinit var pin3 : TextView
    lateinit var pin4 : TextView

    lateinit var btn1 : com.google.android.material.button.MaterialButton
    lateinit var btn2 : com.google.android.material.button.MaterialButton
    lateinit var btn3 : com.google.android.material.button.MaterialButton
    lateinit var btn4 : com.google.android.material.button.MaterialButton
    lateinit var btn5 : com.google.android.material.button.MaterialButton
    lateinit var btn6 : com.google.android.material.button.MaterialButton
    lateinit var btn7 : com.google.android.material.button.MaterialButton
    lateinit var btn8 : com.google.android.material.button.MaterialButton
    lateinit var btn9 : com.google.android.material.button.MaterialButton
    lateinit var btn0 : com.google.android.material.button.MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize ViewBinding and set it as content view
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

}