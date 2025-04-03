package com.example.gofare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvWelcome: TextView
    private lateinit var tvBalance: TextView

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

        displayUserData()
    }

    private fun displayUserData() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            tvWelcome.text = "Welcome $name"
        }

        viewModel.walletBalance.observe(viewLifecycleOwner) { balance ->
            tvBalance.text = "Balance: $balance"
        }
    }


}