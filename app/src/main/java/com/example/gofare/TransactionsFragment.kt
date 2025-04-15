package com.example.gofare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class TransactionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    private val transactionList = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        recyclerView = view.findViewById(R.id.rvTransaction)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup adapter with empty list for now
        adapter = TransactionAdapter(transactionList) { selectedTransaction ->
            // Navigate to ReceiptFragment with the selected transaction
            val receiptFragment = ReceiptFragment.newInstance(selectedTransaction)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, receiptFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = adapter

        getTransactions()

        return view
    }

    private fun getTransactions() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()

        viewModel.rfid.observe(viewLifecycleOwner) { rfid ->
            if (rfid.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Unregistered RFID For User", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updateTransactions(transactions)
        }
    }
}
