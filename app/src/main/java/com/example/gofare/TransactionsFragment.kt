package com.example.gofare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TransactionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        recyclerView = view.findViewById(R.id.rvTransaction)
        recyclerView.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(transactionList)
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

                    Log.d("TransactionFragment", "User RFID: $rfid")  // Log RFID

                    if (rfid != null) {
                        // Fix: Remove quotes around RFID value here
                        val dbRef = FirebaseDatabase.getInstance().getReference("Transactions").child("09d00805").child("transaction1")
                        Log.d("TransactionFragment", "Fetching transactions from path: $dbRef")


                        dbRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val transactionList = mutableListOf<Transaction>()
                                    for (transactionSnapshot in snapshot.children) {
                                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                                        if (transaction != null) {
                                            transactionList.add(transaction)
                                            Log.d("TransactionFragment", "Added transaction: $transaction")
                                        }
                                    }
                                    transactionAdapter.updateTransactions(transactionList)
                                } else {
                                    Log.d("TransactionFragment", "No transactions found.")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("Error", error.message)
                            }
                        })
                    } else {
                        Log.d("TransactionFragment", "RFID is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Error", error.message)
                }
            })
        }
    }




}
