package com.example.gofare

import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class TransactionsFragment : Fragment() {

    private lateinit var nfcAdapter: NfcAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var promptTransaction : LinearLayout

    private val transactionList = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        recyclerView = view.findViewById(R.id.rvTransaction)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = TransactionAdapter(transactionList) { selectedTransaction ->
            val receiptFragment = ReceiptFragment.newInstance(selectedTransaction)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, receiptFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = adapter
        promptTransaction = view.findViewById(R.id.promptTransaction)

        getTransactions()

        return view
    }

    private fun getTransactions() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()

        viewModel.rfid.observe(viewLifecycleOwner) { rfid ->
            if (rfid.isNullOrEmpty() && nfcAdapter == null) {
                Toast.makeText(requireContext(), "NFC is not available on this device", Toast.LENGTH_LONG).show()
                Toast.makeText(requireContext(), "Unregistered RFID For User", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.size == 0) {
                promptTransaction.visibility = View.VISIBLE
            }
            else{
                promptTransaction.visibility = View.GONE
            }
            adapter.updateTransactions(transactions)
        }
    }
}
