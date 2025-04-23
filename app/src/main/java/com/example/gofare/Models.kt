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
    val rfid: String? = null,
    val nfc: String? = null,
    val rfidActive: Boolean? = true,
    val nfcActive: Boolean? = false,
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
    val refunded: Boolean? = false
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

@Parcelize
data class TopUpHistory(
    val topUpId: String? = null,
    val dateTime: String? = null,
    val totalAmount: Double = 0.0,
    val topUpAmount: Double = 0.0,
    val tax: Double = 0.0
): Parcelable

@Parcelize
data class Transit(
    val completedAt: String? = null,
    val createdAt: String? = null,
    val dropoffStop: String? = null,
    val dropoffTime: String? = null,
    val pickupStop: String? = null,
    val pickupTime: String? = null,
    val status: String? = null,
    val updatedAt: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val vehicle: String? = null
): Parcelable


