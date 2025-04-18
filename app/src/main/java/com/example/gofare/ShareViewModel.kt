package com.example.gofare
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import com.google.firebase.firestore.CollectionReference


class SharedViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    private val usersRef = FirebaseFirestore.getInstance().collection("Users")
    private val walletRef = FirebaseFirestore.getInstance().collection("UserWallet")
    private val rfidRef = FirebaseFirestore.getInstance().collection("UserRFID")
    private val transactionRef = FirebaseFirestore.getInstance().collection("UserTransaction")

    private var usersListener: ValueEventListener? = null
    private var transactionListener: ValueEventListener? = null

    // User Data
    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName

    private val _firstName = MutableLiveData<String>()
    val firstName: LiveData<String> get() = _firstName

    private val _lastName = MutableLiveData<String>()
    val lastName: LiveData<String> get() = _lastName

    private val _middleName = MutableLiveData<String>()
    val middleName: LiveData<String> get() = _middleName

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _age = MutableLiveData<String>()
    val age: LiveData<String> get() = _age

    private val _birthday = MutableLiveData<String>()
    val birthday: LiveData<String> get() = _birthday

    private val _contactNumber = MutableLiveData<String>()
    val contactNumber: LiveData<String> get() = _contactNumber

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _gender = MutableLiveData<String>()
    val gender: LiveData<String> get() = _gender

    // User Wallet
    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> get() = _balance

    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> get() = _currency

    private val _loaned = MutableLiveData<String>()
    val loaned: LiveData<String> get() = _loaned

    private val _loanedAmount = MutableLiveData<String>()
    val loanedAmount: LiveData<String> get() = _loanedAmount

    // User Transactions
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    // Current User RFID
    private val _rfid = MutableLiveData<String>()
    val rfid: LiveData<String> get() = _rfid

    fun startLive() {
        if (userId != null) {
            val userRFID = rfidRef.document(userId)
            userRFID.addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("FirebaseData", "Error listening to user data", error)
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    _rfid.value = document.getString("rfid") ?: ""
                    Log.d("FirebaseData", "RFID has been set successfully")
                } else {
                    Log.d("FirebaseData", "User document does not exist")
                }
            }

            observeUserData()
            observeUserTransactions()
            obeserveUserWallet()
        }
    }

    fun observeUserData() {
        if (userId != null) {
            Log.d("User Logged: ", userId)
            val userRef = usersRef.document(userId)
            userRef.addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("FirebaseData", "Error listening to user data", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    _firstName.value = document.getString("firstName") ?: ""
                    _lastName.value = document.getString("lastName") ?: ""
                    _middleName.value = document.getString("middleName") ?: ""
                    _fullName.value = "${_firstName.value} ${_middleName.value} ${_lastName.value}".trim()
                    _address.value = document.getString("address") ?: ""
                    _age.value = document.getLong("age")?.toString() ?: ""
                    _birthday.value = document.getString("birthday") ?: ""
                    _contactNumber.value = document.getString("contactNumber") ?: ""
                    _email.value = document.getString("email") ?: ""
                    _gender.value = document.getString("gender") ?: ""

                    Log.d("FirebaseData", "User data updated successfully")
                } else {
                    Log.d("FirebaseData", "User document does not exist")
                }
            }
        }
    }

    fun obeserveUserWallet() {
        if (userId != null) {
            val walletsRef = walletRef.document(userId)

            walletsRef.addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("FirebaseData", "Error listening to wallet data", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val balance = document.getDouble("balance") ?: 0.0
                    val loanedAmount = document.getDouble("balance") ?: 0.0

                    _balance.value = DecimalFormat("#,##0.00").format(balance)
                    _loanedAmount.value = DecimalFormat("#,##0.00").format(loanedAmount)
                    _currency.value = document.getString("currency")
                    _loaned.value = document.getBoolean("loaned").toString()

                    Log.d("FirebaseData", "Wallet balance updated")
                } else {
                    Log.d("FirebaseData", "Wallet document does not exist")
                }
            }
            Log.d("FirebaseData", "User data updated successfully")
        } else {
            Log.d("FirebaseData", "User document does not exist")
        }
    }

    fun observeUserTransactions() {
        if (userId != null) {
            transactionRef.document(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseData", "Error fetching user transactions", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val allTransactions = mutableListOf<Transaction>()
                    for ((transactionId, value) in snapshot.data ?: emptyMap()) {
                        val data = value as? Map<*, *> ?: continue
                        val transaction = Transaction(
                            transactionId = transactionId,
                            pickup = data["pickup"] as? String ?: "",
                            dropoff = data["dropoff"] as? String ?: "",
                            currentBalance = (data["currentBalance"] as? Number)?.toDouble() ?: 0.0,
                            remainingBalance = (data["remainingBalance"] as? Number)?.toDouble()
                                ?: 0.0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                            discount = data["discount"] as? Boolean ?: false,
                            loaned = data["loaned"] as? Boolean ?: false,
                            loanedAmount = (data["loanedAmount"] as? Number)?.toDouble() ?: 0.0,
                            dateTime = data["dateTime"] as? String ?: ""
                        )
                        allTransactions.add(transaction)
                    }
                    _transactions.value = allTransactions
                    Log.d("FirebaseData", "Observed ${allTransactions.size} transactions.")
                } else {
                    Log.d("FirebaseData", "Document doesn't exist for userId: $userId")
                }
            }
        }
        else {
            Log.d("FirebaseData", "User document does not exist")
        }
    }
}
