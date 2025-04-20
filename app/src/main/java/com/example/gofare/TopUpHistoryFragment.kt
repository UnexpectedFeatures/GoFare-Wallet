package com.example.gofare

import android.nfc.NfcAdapter
import android.os.Bundle
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

class TopUpHistoryFragment : Fragment() {

    private lateinit var nfcAdapter: NfcAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TopUpHistoryAdapter
    private lateinit var promptTopUp: LinearLayout

    private val topUpHistoryList = mutableListOf<TopUpHistory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        val view = inflater.inflate(R.layout.fragment_top_up_history, container, false)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        recyclerView = view.findViewById(R.id.topUpHistoryRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = TopUpHistoryAdapter(topUpHistoryList) { selectedTopUp ->
            // OPTIONAL: Add details fragment if needed
            val receiptFragment = TopUpReceiptFragment.newInstance(selectedTopUp)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, receiptFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = adapter
//        promptTopUp = view.findViewById(R.id.promptTopUp)

        getTopUpHistory()

        return view
    }

    private fun getTopUpHistory() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()

        viewModel.topUp.observe(viewLifecycleOwner) { history ->
            adapter.updateTopUpHistory(history)
        }
    }
}
