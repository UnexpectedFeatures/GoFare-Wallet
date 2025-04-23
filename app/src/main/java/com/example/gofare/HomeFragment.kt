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
import androidx.lifecycle.lifecycleScope
import com.google.type.DateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvWelcome: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvCurrency: TextView
    private lateinit var topUpBtn: ImageButton
    private lateinit var scanNFCBtn: ImageButton

    private lateinit var pickup: TextView
    private lateinit var dropoff: TextView
    private lateinit var total: TextView
    private lateinit var dateTime: TextView
    private lateinit var transitTv: TextView

    private lateinit var viewModel: SharedViewModel
    private var transitAnimationJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Please Log Out in the settings", Toast.LENGTH_SHORT).show()
        }

        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvBalance = view.findViewById(R.id.tvBalance)
        tvCurrency = view.findViewById(R.id.tvCurrency)
        topUpBtn = view.findViewById(R.id.topUpBtn)
        scanNFCBtn = view.findViewById(R.id.scanNFCBtn)

        pickup = view.findViewById(R.id.pickup)
        dropoff = view.findViewById(R.id.dropoff)
        total = view.findViewById(R.id.total)
        dateTime = view.findViewById(R.id.dateTime)

        transitTv = view.findViewById(R.id.transitTv)

        topUpBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TopUpFragment())
                .commit()
        }
        scanNFCBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanFragment())
                .commit()
        }

        displayUserData()
        displayOverview()
    }

    private fun displayOverview() {
        viewModel.startLive()

        viewModel.transactions.observe(viewLifecycleOwner) { transactionsList ->
            Log.d("Transaction Display", "Displaying Transactions")
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


        viewModel.transit.observe(viewLifecycleOwner) { transit ->
            var isActive = false
            if (transit.isNotEmpty()){
                isActive = true
                val texts = listOf("In Transit   ", "In Transit.  ", "In Transit.. ", "In Transit...")

                transitAnimationJob = lifecycleScope.launch {
                    while (isActive) {
                        for (text in texts) {
                            transitTv.text = text
                            delay(500)
                        }
                    }
                }
                transitTv.visibility = View.VISIBLE
            }
            else{
                isActive = false
                stopTransitTextLoop()
                transitTv.visibility = View.GONE
            }
        }
    }

    private fun stopTransitTextLoop() {
        transitAnimationJob?.cancel()
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