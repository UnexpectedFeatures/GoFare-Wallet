package com.example.gofare

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider

class ReceiptFragment : Fragment() {

    private lateinit var transactionId : TextView
    private lateinit var pickup : TextView
    private lateinit var dropoff : TextView
    private lateinit var loaned : TextView
    private lateinit var dateTime : TextView
    private lateinit var balance : TextView
    private lateinit var total : TextView
    private lateinit var remainingBalance : TextView

    companion object {
        fun newInstance(transaction: Transaction): ReceiptFragment {
            val fragment = ReceiptFragment()
            val bundle = Bundle()
            bundle.putParcelable("selectedTransaction", transaction)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_receipt, container, false)

        transactionId = view.findViewById(R.id.transactionId)
        pickup = view.findViewById(R.id.pickUp)
        dropoff = view.findViewById(R.id.dropOff)
        loaned = view.findViewById(R.id.loaned)
        dateTime = view.findViewById(R.id.dateTime)
        balance = view.findViewById(R.id.balance)
        total = view.findViewById(R.id.total)
        remainingBalance = view.findViewById(R.id.remainingBalance)

        val transaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("selectedTransaction", Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("selectedTransaction")
        }

        if (transaction == null) {
            Toast.makeText(requireContext(), "Transaction not found", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()

        Log.d("Transaction: ", transaction.toString())

        if (transaction?.loaned == false){
            loaned.visibility = View.GONE

        }
        else {
            loaned.visibility = View.VISIBLE
        }
        viewModel.currency.observe(viewLifecycleOwner) { currency ->


            transactionId.text = transaction?.transactionId
            pickup.text = transaction?.pickup
            dropoff.text = transaction?.dropoff
            loaned.text = "Loaned: " + transaction?.loaned.toString()
            dateTime.text = "Date: " + transaction?.dateTime
            balance.text = "$currency " + transaction?.currentBalance.toString()
            total.text = "$currency " + transaction?.totalAmount.toString()
            remainingBalance.text = "$currency " + transaction?.remainingBalance.toString()

        }
        return view
    }

}