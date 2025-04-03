package com.example.gofare
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import java.text.DecimalFormat

class SharedViewModel : ViewModel() {
    private val dbRef = FirebaseDatabase.getInstance().getReference("ClientReference")
    private var databaseListener: ValueEventListener? = null

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _walletBalance = MutableLiveData<String>()
    val walletBalance: LiveData<String> get() = _walletBalance

    private val _walletCurrency = MutableLiveData<String>()
    val walletCurrency: LiveData<String> get() = _walletCurrency

    fun observeUserData(userId: String) {
        val userRef = dbRef.child(userId)

        // Remove existing listener if any (to avoid duplicates)
        databaseListener?.let { userRef.removeEventListener(it) }

        databaseListener = userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val balance = snapshot.child("wallet/balance").getValue(Double::class.java) ?: 0.00
                    val currency = snapshot.child("wallet/currency").getValue(String::class.java) ?: "PHP"
                    val decimalFormat = DecimalFormat("#,###.##")
                    val formattedBalance = decimalFormat.format(balance)
                    _userName.value = "$firstName $lastName"
                    _walletBalance.value = formattedBalance
                    _walletCurrency.value = currency
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Error Database", error.toString())
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        databaseListener?.let { dbRef.removeEventListener(it) }
    }
}
