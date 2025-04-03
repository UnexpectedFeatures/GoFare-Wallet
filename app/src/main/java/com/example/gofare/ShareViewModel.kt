package com.example.gofare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _walletBalance = MutableLiveData<String>()
    val walletBalance: LiveData<String> get() = _walletBalance

    fun setUserData(name: String, balance: String) {
        _userName.value = name
        _walletBalance.value = balance
    }
}
