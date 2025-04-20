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
import com.google.firebase.firestore.FirebaseFirestore
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

        binding.backBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        binding.historyBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TopUpHistoryFragment())
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

                    // Handle the response from backend (get clientSecret)
                    val clientSecret = response.getString("clientSecret")

                    // Initialize Stripe SDK
                    val stripe = Stripe(requireContext(), "pk_test_51RFeoeKdsoqgvHZObprE6d41umUgO3claXEi7QKzc8lX4LYlLs2v27CISz6XGrqEktrLvL3cWwml4cgr4Vlf8dB600SEzYpv6Q") // Replace with your actual public key

                    // Create PaymentIntent params (you can collect payment method from the Stripe Elements in a real app)
                    val paymentMethodId = "pm_card_visa"
                    val paymentIntentParams = ConfirmPaymentIntentParams.createWithPaymentMethodId(paymentMethodId, clientSecret)

                    // Confirm the payment intent with the Stripe SDK
                    stripe.confirmPayment(this, paymentIntentParams)

                    // Show the AlertDialog after payment is confirmed
                    val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Top-Up")
                        .setMessage("Are you sure you want to top up ₱$totalDeposit?")
                        .setPositiveButton("Yes") { _, _ ->
                            // Handle successful top-up and update Firebase
                            val auth = FirebaseAuth.getInstance()
                            val userId = auth.currentUser?.uid

                            if (userId != null) {
                                val dbRef = FirebaseFirestore.getInstance().collection("UserWallet").document(userId)

                                dbRef.get().addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        val currentBalance = documentSnapshot.getDouble("balance") ?: 0.0
                                        val loaned = documentSnapshot.getBoolean("loaned") ?: false
                                        val newBalance = currentBalance + totalDeposit.toDouble()

                                        val updates = hashMapOf<String, Any>(
                                            "balance" to newBalance,
                                            "lastUpdated" to System.currentTimeMillis()
                                        )

                                        if (loaned) {
                                            updates["loaned"] = false
                                        }

                                        dbRef.update(updates)
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
                    Log.d("Server Error", error.message.toString())
                    Toast.makeText(requireContext(), "Server Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    return headers
                }

                override fun getBody(): ByteArray {
                    val params = JSONObject()
                    params.put("userId", FirebaseAuth.getInstance().currentUser?.uid ?: "")
                    params.put("amount", totalDeposit)
                    return params.toString().toByteArray()
                }
            }

            val requestQueue = Volley.newRequestQueue(requireContext())
            requestQueue.add(postRequest)
        }
    }
}