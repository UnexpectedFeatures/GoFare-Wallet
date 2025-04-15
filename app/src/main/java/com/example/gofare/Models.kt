package com.example.gofare

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Clients(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val age: Int? = null,
    val birthday: String? = null,
    val gender: String? = null,
    val contactNumber: String? = null,
    val address: String? = null,
)

data class Wallet(
    val balance: Double = 0.0,
    val currency: String? = null,
    val loanedAmount: Double = 0.0,
    val loaned: Boolean = false
)

data class RFID(
    val rfid: String = ""
)

@Parcelize
data class Transaction(
    val currentBalance: Double = 0.0,
    val dateTime: String = "4-13-2025 5:08 PM",
    val discount: Boolean = false,
    val dropoff: String = "Vicas",
    val loaned: Boolean = false,
    val loanedAmount: Double = 0.0,
    val pickup: String = "Kiko",
    val remainingBalance: Double = 1000.0,
    val totalAmount: Double = 13.0,
    val transactionId: String = "TX-0125215"
): Parcelable