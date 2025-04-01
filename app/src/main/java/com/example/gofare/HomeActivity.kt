package com.example.gofare

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class HomeActivity : AppCompatActivity() {
    private var balance: Double = 1000.0
    private lateinit var txtBalance: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        txtBalance = findViewById(R.id.txtBalance)
        val btnWithdraw: Button = findViewById(R.id.btnWithdraw)
        val btnDeposit: Button = findViewById(R.id.btnDeposit)

        btnWithdraw.setOnClickListener {
            if (balance >= 100) {
                balance -= 100
                updateBalance()
                Toast.makeText(this, "Withdrawn $100", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeposit.setOnClickListener {
            balance += 100
            updateBalance()
            Toast.makeText(this, "Deposited $100", Toast.LENGTH_SHORT).show()
        }

       // btnSettings.setOnClickListener {
      //      Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
       // }
    }

    private fun updateBalance() {
        txtBalance.text = "Balance: $$balance"
    }

}