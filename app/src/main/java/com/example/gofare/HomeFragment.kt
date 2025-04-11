package com.example.gofare

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.type.DateTime


class HomeFragment : Fragment(R.layout.fragment_home) {


    private lateinit var tvWelcome: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvCurrency: TextView
    private lateinit var topUpBtn: ImageButton

    private lateinit var transactionCount: TextView
    private lateinit var pickup: TextView
    private lateinit var dropoff: TextView
    private lateinit var total: TextView
    private lateinit var dateTime: TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvBalance = view.findViewById(R.id.tvBalance)
        tvCurrency = view.findViewById(R.id.tvCurrency)
        topUpBtn = view.findViewById(R.id.topUpBtn)

        transactionCount = view.findViewById(R.id.transactionCount)
        pickup = view.findViewById(R.id.pickup)
        dropoff = view.findViewById(R.id.dropoff)
        total = view.findViewById(R.id.total)
        dateTime = view.findViewById(R.id.dateTime)

        topUpBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TopUpFragment())
                .commit()
        }


        displayUserData()
        displayOverview()
    }

    private fun displayOverview() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        viewModel.transactions.observe(viewLifecycleOwner) { transactionsList ->
            transactionCount.text = "Transactions: ${transactionsList.size}"

            if (transactionsList.isNotEmpty()) {
                val lastTransaction = transactionsList[transactionsList.size - 1]

                pickup.text = "Pickup: ${lastTransaction.pickup ?: "Unknown"}"
                dropoff.text = "Dropoff: ${lastTransaction.dropoff ?: "Unknown"}"
                total.text = "Total: ₱${lastTransaction.total ?: "0.00"}"
                dateTime.text = "Date: ${lastTransaction.date ?: "N/A"} | ${lastTransaction.time}"
            } else {
                pickup.text = "Pickup: -"
                dropoff.text = "Dropoff: -"
                total.text = "Total: -"
                dateTime.text = "Date: -"
            }
        }
    }

    private fun displayUserData() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        viewModel.firstName.observe(viewLifecycleOwner) { name ->
            tvWelcome.text = "Welcome $name"
        }

        viewModel.walletBalance.observe(viewLifecycleOwner) { balance ->
            if (balance.toString().length > 16) {
                val textSizeInSp = 24f
                val textSizeInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, textSizeInSp, resources.displayMetrics
                )
                tvBalance.textSize = textSizeInPx / resources.displayMetrics.density
            }
            else{
                val textSizeInSp = 40f
                val textSizeInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, textSizeInSp, resources.displayMetrics
                )
                tvBalance.textSize = textSizeInPx / resources.displayMetrics.density
            }

            tvBalance.text = balance.toString()

        }


        viewModel.walletCurrency.observe(viewLifecycleOwner) { currency ->
            tvCurrency.text = "$currency"
        }

    }


}