package com.example.gofare

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.Manifest
import androidx.core.content.ContextCompat


class TransactionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()
    private val previousTransactionIds = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        recyclerView = view.findViewById(R.id.rvTransaction)
        recyclerView.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(transactionList, object : TransactionAdapter.OnItemClickListener {
            override fun onTransactionClick(transaction: Transaction) {
                // Optional: Pass data to ReceiptFragment using arguments or ViewModel
                val receiptFragment = ReceiptFragment.newInstance(transaction)

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, receiptFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
        recyclerView.adapter = transactionAdapter

        fetchTransactionData()

        return view
    }

    private fun fetchTransactionData() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("ClientReference").child(userId)

            userRef.child("rfid").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rfid = snapshot.getValue(String::class.java)

                    if (!rfid.isNullOrEmpty()) {
                        val dbRef = FirebaseDatabase.getInstance().getReference("Transactions").child(rfid)

                        dbRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val newList = mutableListOf<Transaction>()
                                    val newTransactionIds = mutableSetOf<String>()

                                    for (transactionSnapshot in snapshot.children) {
                                        val transactionId = transactionSnapshot.key
                                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                                        if (transaction != null) {
                                            val transactionWithId = transaction.copy(transactionId = transactionId)
                                            newList.add(transactionWithId)
                                            newTransactionIds.add(transactionId ?: "")
                                        }
                                    }

                                    // Compare new transaction IDs with the previous ones
                                    val newTransactions = newTransactionIds.subtract(previousTransactionIds)

                                    // If new transactions exist, notify
                                    if (newTransactions.isNotEmpty()) {
                                        sendNotification("New Transaction", "You have a new transaction.")
                                    }

                                    // Update previousTransactionIds to current ones
                                    previousTransactionIds.clear()
                                    previousTransactionIds.addAll(newTransactionIds)

                                    // Update the transaction list and refresh adapter
                                    transactionList.clear()
                                    transactionList.addAll(newList)
                                    transactionAdapter.updateTransactions(transactionList)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("Error", error.message)
                            }
                        })
                    } else {
                        Toast.makeText(requireContext(), "User RFID Not Registered! Redirected to Home Page", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Error", error.message)
                }
            })
        }
    }

    private fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(requireContext(), "transaction_channel")
            .setSmallIcon(R.drawable.go_fare_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // No need to check permission here, handle in the onRequestPermissionsResult method
        with(NotificationManagerCompat.from(requireContext())) {
            notify(1, builder.build())
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendNotification("Permission Granted", "You will now receive transaction alerts.")
        } else {
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            } else {
                sendNotification("Permission Granted", "You will now receive transaction alerts.")
            }
        } else {
            sendNotification("Permission Granted", "You will now receive transaction alerts.")
        }
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestPermission()
    }
}
