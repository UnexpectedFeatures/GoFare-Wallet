package com.example.gofare

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import com.google.type.DateTime


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvWelcome: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvCurrency: TextView
    private lateinit var topUpBtn: ImageButton

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Please Log Out in the settings", Toast.LENGTH_SHORT).show()
        }

        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvBalance = view.findViewById(R.id.tvBalance)
        tvCurrency = view.findViewById(R.id.tvCurrency)
        topUpBtn = view.findViewById(R.id.topUpBtn)

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

        viewModel.startLive()

        viewModel.transactions.observe(viewLifecycleOwner) { transactionsList ->
            if (transactionsList.isNotEmpty()) {
                val lastTransaction = transactionsList[transactionsList.size - 1]
                pickup.text = "Pickup: ${lastTransaction.pickup ?: "Unknown"}"
                dropoff.text = "Dropoff: ${lastTransaction.dropoff ?: "Unknown"}"
                total.text = "Total: ₱${lastTransaction.totalAmount ?: "0.00"}"
                dateTime.text = "Date: ${lastTransaction.dateTime ?: "N/A"}"
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

        viewModel.startLive()

        viewModel.firstName.observe(viewLifecycleOwner) { name ->
            tvWelcome.text = "Welcome $name"
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            if (balance.length > 5){
                tvBalance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            }
            else{
                tvBalance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            }
            tvBalance.text = "₱$balance"
        }

        viewModel.currency.observe(viewLifecycleOwner) { currency ->
            tvCurrency.text = currency ?: "PHP"
        }
    }




}