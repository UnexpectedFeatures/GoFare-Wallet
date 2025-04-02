package com.example.gofare

data class Clients(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val contactNumber: String? = null,
    val address: String? = null,
    val wallet: Wallet? = null,
    val accountStatus: String? = null
)

data class Wallet(
    val balance: Double = 0.0,
    val currency: String = "PHP",
    val loanedAmount: Double = 0.0,
    val status: String = "default"
)
