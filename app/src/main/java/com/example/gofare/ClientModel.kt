package com.example.gofare

data class Clients(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val contactNumber: String? = null,
    val address: String? = null,
    val rfid: String? = null,
    val wallet: Wallet? = null,
    val accountStatus: String? = null
)

data class Wallet(
    val balance: Double = 0.0,
    val currency: String = "PHP",
    val loanedAmount: Double = 0.0,
    val status: String = "default"
)

data class Transaction(
    val transactionId: String? = null,
    val discount: Boolean = false,
    val loaned: Boolean = false,
    val balance: Double = 0.0,
    val total: Double = 0.0,
    val remBalance: Double = 0.0,
    val pickUp: String = "",
    val dropOff: String = "",
    val date: String = "",
    val time: String = ""
)