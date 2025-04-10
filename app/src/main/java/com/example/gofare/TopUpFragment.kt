package com.example.gofare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.gofare.databinding.FragmentTopUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TopUpFragment : Fragment() {

    lateinit var binding : FragmentTopUpBinding

    private lateinit var backBtn : com.google.android.material.button.MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backBtn = view.findViewById(R.id.backBtn)
        backBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        assignId(binding.creditsA)
        assignId(binding.creditsB)
        assignId(binding.creditsC)
        assignId(binding.creditsD)
        assignId(binding.creditsE)

    }
    private fun assignId(btn: com.google.android.material.button.MaterialButton) {

        btn.setOnClickListener {
            val totalDeposit = btn.text.toString().substring(3).trim()

            if (totalDeposit.isEmpty()) {
                Toast.makeText(requireContext(), "Invalid deposit amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Top-Up")
                .setMessage("Are you sure you want to top up ₱$totalDeposit?")
                .setPositiveButton("Yes") { _, _ ->

                    val auth = FirebaseAuth.getInstance()
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        val dbRef = FirebaseDatabase.getInstance().getReference("ClientReference").child(userId)
                        val walletRef = dbRef.child("wallet")

                        walletRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val currentBalance = snapshot.child("balance").getValue(Double::class.java) ?: 0.0
                                val status = snapshot.child("status").getValue(String::class.java) ?: "default"
                                val newBalance = currentBalance + totalDeposit.toDouble()

                                val updates = mutableMapOf<String, Any>(
                                    "balance" to newBalance,
                                    "lastUpdated" to System.currentTimeMillis()
                                )

                                if (status == "loaned") {
                                    updates["status"] = "default"
                                }

                                walletRef.updateChildren(updates)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Wallet updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Failed to update wallet", Toast.LENGTH_SHORT).show()
                                    }

                            } else {
                                Toast.makeText(requireContext(), "Wallet not found", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to read wallet", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                .setNegativeButton("No", null)
                .create()

            alertDialog.show()
        }
    }


}