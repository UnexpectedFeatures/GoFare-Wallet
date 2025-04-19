package com.example.gofare

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.gofare.databinding.FragmentTopUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import org.json.JSONObject


class TopUpFragment : Fragment() {

    private lateinit var requestQueue: RequestQueue
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
        requestQueue = Volley.newRequestQueue(requireContext())

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

            // Send the deposit amount to your backend for Stripe payment intent
            val url = "http://10.0.2.2:3000/create-payment-intent"

            val postRequest = object : JsonObjectRequest(
                Method.POST, url, null,
                Response.Listener { response ->

                    // Handle the response
                    val clientSecret = response.getString("clientSecret")

                    val stripe = Stripe(requireContext(), "pk_test_51RFeoeKdsoqgvHZObprE6d41umUgO3claXEi7QKzc8lX4LYlLs2v27CISz6XGrqEktrLvL3cWwml4cgr4Vlf8dB600SEzYpv6Q") // Use your actual public key here

                    // Create the PaymentIntent using the clientSecret
                    val paymentIntentParams = ConfirmPaymentIntentParams.createWithPaymentMethodId(
                        "pm_card_visa",  // Replace with actual payment method ID (can be collected using Stripe Elements)
                        clientSecret
                    )

                    // Confirm the payment on the client
                    stripe.confirmPayment(this, paymentIntentParams)

                    // Show the AlertDialog after payment is confirmed
                    val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Top-Up")
                        .setMessage("Are you sure you want to top up ₱$totalDeposit?")
                        .setPositiveButton("Yes") { _, _ ->
                            // Handle successful top-up and update Firebase as before
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


                },
                Response.ErrorListener { error ->
                    // Handle error
                    Log.d("Server Error", error.message.toString())
                    Toast.makeText(requireContext(), "Server Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"  // Set the Content-Type header to application/json
                    return headers
                }

                override fun getBody(): ByteArray {
                    val params = JSONObject()
                    params.put("userId", FirebaseAuth.getInstance().currentUser?.uid ?: "")
                    params.put("amount", totalDeposit)
                    return params.toString().toByteArray()  // Convert the JSON to byte array
                }
            }

            val requestQueue = Volley.newRequestQueue(requireContext())
            requestQueue.add(postRequest)
        }


//        btn.setOnClickListener {
//            val totalDeposit = btn.text.toString().substring(3).trim()
//
//            if (totalDeposit.isEmpty()) {
//                Toast.makeText(requireContext(), "Invalid deposit amount", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                .setTitle("Confirm Top-Up")
//                .setMessage("Are you sure you want to top up ₱$totalDeposit?")
//                .setPositiveButton("Yes") { _, _ ->
//
//                    val auth = FirebaseAuth.getInstance()
//                    val userId = auth.currentUser?.uid
//
//                    if (userId != null) {
//                        val dbRef = FirebaseDatabase.getInstance().getReference("ClientReference").child(userId)
//                        val walletRef = dbRef.child("wallet")
//
//                        walletRef.get().addOnSuccessListener { snapshot ->
//                            if (snapshot.exists()) {
//                                val currentBalance = snapshot.child("balance").getValue(Double::class.java) ?: 0.0
//                                val status = snapshot.child("status").getValue(String::class.java) ?: "default"
//                                val newBalance = currentBalance + totalDeposit.toDouble()
//
//                                val updates = mutableMapOf<String, Any>(
//                                    "balance" to newBalance,
//                                    "lastUpdated" to System.currentTimeMillis()
//                                )
//
//                                if (status == "loaned") {
//                                    updates["status"] = "default"
//                                }
//
//                                walletRef.updateChildren(updates)
//                                    .addOnSuccessListener {
//                                        Toast.makeText(requireContext(), "Wallet updated successfully", Toast.LENGTH_SHORT).show()
//                                    }
//                                    .addOnFailureListener {
//                                        Toast.makeText(requireContext(), "Failed to update wallet", Toast.LENGTH_SHORT).show()
//                                    }
//
//                            } else {
//                                Toast.makeText(requireContext(), "Wallet not found", Toast.LENGTH_SHORT).show()
//                            }
//                        }.addOnFailureListener {
//                            Toast.makeText(requireContext(), "Failed to read wallet", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                }
//                .setNegativeButton("No", null)
//                .create()
//
//            alertDialog.show()
//        }
    }


}