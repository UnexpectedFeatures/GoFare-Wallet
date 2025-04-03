package com.example.gofare

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvWelcome: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvCurrency: TextView

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


        displayUserData()
    }

    private fun displayUserData() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        viewModel.userName.observe(viewLifecycleOwner) { name ->
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

            tvBalance.text = "$balance"
        }


        viewModel.walletCurrency.observe(viewLifecycleOwner) { currency ->
            tvCurrency.text = "$currency"
        }

    }


}