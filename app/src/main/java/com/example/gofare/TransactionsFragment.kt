package com.example.gofare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

                    if (!rfid.isNullOrEmpty())  {
                        // Fix: Remove quotes around RFID value here
                        val dbRef = FirebaseDatabase.getInstance().getReference("Transactions").child(rfid)
                        Log.d("TransactionFragment", "Fetching transactions from path: $dbRef")

                        dbRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val transactionList = mutableListOf<Transaction>()
                                    for (transactionSnapshot in snapshot.children) {
                                        val transactionId = transactionSnapshot.key
                                        val transaction = transactionSnapshot.getValue(Transaction::class.java)

                                        if (transaction != null) {
                                            val transactionWithId = transaction.copy(transactionId = transactionId)
                                            transactionList.add(transactionWithId)
                                            Log.d("TransactionFragment", "Added transaction: $transactionWithId")
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
                        Toast.makeText(requireContext(), "User RFID Not Registered! Redirected to Home Page", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                        return
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Error", error.message)
                }
            })
        }
    }




}
