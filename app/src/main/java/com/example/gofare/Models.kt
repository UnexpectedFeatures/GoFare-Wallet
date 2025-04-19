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
    val dateTime: String? = null,
    val discount: Boolean = false,
    val dropoff: String? = null,
    val loaned: Boolean = false,
    val loanedAmount: Double = 0.0,
    val pickup: String? = null,
    val remainingBalance: Double = 0.0,
    val totalAmount: Double = 0.0,
    val transactionId: String? = null,
): Parcelable

@Parcelize
data class UserRequest(
    val date: String? = null,
    val time: String? = null,
    val description: String? = null,
    val reason: String? = null,
    val status: String? = null,
    val type: String? = null,
    val requestId: String? = null,
): Parcelable