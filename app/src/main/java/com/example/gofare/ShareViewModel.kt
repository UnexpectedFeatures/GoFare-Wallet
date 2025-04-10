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
    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName

    private val _firstName = MutableLiveData<String>()
    val firstName: LiveData<String> get() = _firstName

    private val _lastName = MutableLiveData<String>()
    val lastName: LiveData<String> get() = _lastName

    private val _middleName = MutableLiveData<String>()
    val middleName: LiveData<String> get() = _middleName

    private val _accountStatus = MutableLiveData<String>()
    val accountStatus: LiveData<String> get() = _accountStatus

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _age = MutableLiveData<String>()
    val age: LiveData<String> get() = _age

    private val _contactNumber = MutableLiveData<String>()
    val contactNumber: LiveData<String> get() = _contactNumber

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _gender = MutableLiveData<String>()
    val gender: LiveData<String> get() = _gender

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
                    val rfid = snapshot.child("rfid").getValue(String::class.java) ?: ""

                    val firstName = snapshot.child("firstName").getValue(String::class.java)
                    val lastName = snapshot.child("lastName").getValue(String::class.java)
                    val middleName = snapshot.child("middleName").getValue(String::class.java)
                    val accountStatus = snapshot.child("accountStatus").getValue(String::class.java)
                    val address = snapshot.child("address").getValue(String::class.java)
                    val age = snapshot.child("age").getValue(String::class.java)
                    val contactNumber = snapshot.child("contactNumber").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val gender = snapshot.child("gender").getValue(String::class.java)
                    val balance = snapshot.child("wallet/balance").getValue(Double::class.java) ?: 0.00
                    val currency = snapshot.child("wallet/currency").getValue(String::class.java) ?: "PHP"

                    val formattedBalance = DecimalFormat("#,###.00").format(balance)

                    _fullName.value = "$firstName ${middleName?.substring(0, 1)}. $lastName"
                    _firstName.value = firstName ?: "Unknown"
                    _lastName.value = lastName ?: "Unknown"
                    _middleName.value = middleName ?: "Unknown"
                    _accountStatus.value = accountStatus ?: "Unknown"
                    _address.value = address ?: "Unknown"
                    _age.value = age ?: "Unknown"
                    _contactNumber.value = contactNumber ?: "Unknown"
                    _email.value = email ?: "Unknown"
                    _gender.value = gender ?: "Unknown"

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
