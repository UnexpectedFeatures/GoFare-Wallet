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
    private val transactionRef = FirebaseDatabase.getInstance().getReference("Transactions")

    private var userListener: ValueEventListener? = null
    private var transactionListener: ValueEventListener? = null

    // User Info
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _walletBalance = MutableLiveData<String>()
    val walletBalance: LiveData<String> get() = _walletBalance

    private val _walletCurrency = MutableLiveData<String>()
    val walletCurrency: LiveData<String> get() = _walletCurrency

    // Transactions
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    fun observeUserData(userId: String) {
        val userRef = dbRef.child(userId)

        // Remove existing listener if any (to avoid duplicates)
        userListener?.let { userRef.removeEventListener(it) }

        userListener = userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val balance = snapshot.child("wallet/balance").getValue(Double::class.java) ?: 0.00
                    val currency = snapshot.child("wallet/currency").getValue(String::class.java) ?: "PHP"
                    val rfid = snapshot.child("rfid").getValue(String::class.java) ?: ""

                    val formattedBalance = DecimalFormat("#,###.##").format(balance)

                    _userName.value = "$firstName $lastName"
                    _walletBalance.value = formattedBalance
                    _walletCurrency.value = currency

                    if (rfid.isNotEmpty()){
                        loadTransactions(rfid)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Error Database", error.toString())
            }
        })
    }

    private fun loadTransactions(rfid: String) {
        val cleanedRfid = rfid.replace("\"", "")

        transactionListener?.let {
            transactionRef.child(cleanedRfid).removeEventListener(it)
        }

        val userTransactionsRef = transactionRef.child(cleanedRfid)

        transactionListener = userTransactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionList = mutableListOf<Transaction>()
                for (child in snapshot.children) {
                    val transaction = child.getValue(Transaction::class.java)
                    transaction?.let { transactionList.add(it) }
                }
                _transactions.value = transactionList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("SharedViewModel", "Transaction Error: ${error.message}")
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.let { dbRef.removeEventListener(it) }
        transactionListener?.let { transactionRef.removeEventListener(it) }
    }
}
