package com.example.gofare

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

class TopUpReceiptFragment : Fragment() {

    private lateinit var topUpId: TextView
    private lateinit var totalAmount: TextView
    private lateinit var totalCost: TextView
    private lateinit var dateTime: TextView
    private lateinit var tax: TextView

    companion object {
        fun newInstance(topUp: TopUpHistory): TopUpReceiptFragment {
            val fragment = TopUpReceiptFragment()
            val bundle = Bundle()
            bundle.putParcelable("selectedTopUp", topUp)
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
        val view = inflater.inflate(R.layout.fragment_top_up_receipt, container, false)

        topUpId = view.findViewById(R.id.topUpId)
        totalCost = view.findViewById(R.id.totalCost)
        totalAmount = view.findViewById(R.id.totalAmount)
        tax = view.findViewById(R.id.tax)
        dateTime = view.findViewById(R.id.dateTime)

        val topUp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("selectedTopUp", TopUpHistory::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("selectedTopUp")
        }

        if (topUp == null) {
            Toast.makeText(requireContext(), "TopUp not found", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()

        Log.d("TopUp: ", topUp.toString())

        viewModel.currency.observe(viewLifecycleOwner) { currency ->
            topUpId.text = topUp?.topUpId.toString()
            totalCost.text = "$currency " + topUp?.totalAmount.toString()
            totalAmount.text = "$currency " + topUp?.topUpAmount.toString()
            tax.text = "$currency " + topUp?.tax.toString()
            dateTime.text = "Date: ${topUp?.dateTime}"

        }
        return view
    }
}

