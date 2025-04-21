package com.example.gofare

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gofare.databinding.FragmentTopUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Objects
import kotlin.math.ceil
import okio.ByteString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response




class TopUpFragment : Fragment() {

    private lateinit var requestQueue: RequestQueue
    lateinit var binding : FragmentTopUpBinding

    lateinit var totalDeposit : String

    private lateinit var paymentSheet: com.stripe.android.paymentsheet.PaymentSheet

    private lateinit var clientSecret: String


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

        PaymentConfiguration.init(
            requireContext(),
            "pk_test_51RFWOBBLDZkRasaW9U1JVUm4b00HYbwwGuOWi2RZy9g4C58BshODQfbuX6akLQRA967OMqzwIDzAbtwwfYCbh5u800D1xCS6Db"
        )

        // Then initialize your PaymentSheet
        paymentSheet = PaymentSheet(this) { paymentResult ->
            onPaymentSheetResult(paymentResult)
        }

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

    private fun assignId(btn: MaterialButton) {
        btn.setOnClickListener {
            totalDeposit = btn.text.toString().substring(3).trim()

            if (totalDeposit.isEmpty()) {
                Toast.makeText(requireContext(), "Invalid deposit amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                    Toast.makeText(requireContext(), "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                val fcmToken = task.result
                val phpAmount = totalDeposit.toDouble()
                val usdAmount = ceil(phpAmount / 56)
                val totalAmountWithFee = usdAmount + ceil(usdAmount * 0.10)

                // THIS IS CAPS SO ITS EASIER TO NOTICE
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("ws://172.20.10.5:3003")
                    .build()

                val webSocketListener = WebsocketConnection(requireActivity(), paymentSheet)
                val webSocket = client.newWebSocket(request, webSocketListener)

                val socketMessage = JSONObject().apply {
                    put("event", "createPayment")
                    put("userId", FirebaseAuth.getInstance().currentUser?.uid ?: "")
                    put("amount", totalAmountWithFee.toInt())
                    put("fcmToken", fcmToken)
                }

                webSocket.send(socketMessage.toString())

            }
        }
    }

    private fun onPaymentSheetResult(paymentResult: com.stripe.android.paymentsheet.PaymentSheetResult) {
        when (paymentResult) {
            is com.stripe.android.paymentsheet.PaymentSheetResult.Completed -> {
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    val dbRef = FirebaseFirestore.getInstance().collection("UserWallet").document(userId)
                    val topUpRef = FirebaseFirestore.getInstance().collection("UserTopUp").document(userId)

                    dbRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val currentBalance = documentSnapshot.getDouble("balance") ?: 0.0
                            val loaned = documentSnapshot.getBoolean("loaned") ?: false
                            val newBalance = currentBalance + totalDeposit.toDouble()

                            val updates = hashMapOf<String, Any>(
                                "balance" to newBalance,
                                "lastUpdated" to System.currentTimeMillis()
                            )

                            val phpAmount = totalDeposit.toDouble()
                            val usdAmount = ceil(phpAmount / 56)
                            val taxedUsd = usdAmount * 0.10

                            val tax = ceil(taxedUsd * 56)
                            val topUpAmount = ceil(usdAmount * 56)
                            val totalCost = ceil((usdAmount + taxedUsd) * 56)

                            topUpRef.get().addOnSuccessListener { snapshot ->
                                val existingTopup = snapshot.data ?: emptyMap<String, Any>()
                                val count = existingTopup.size
                                val topUpId = "TU-" + String.format("%04d", count + 1)

                                val timestamp = Timestamp.now()
                                val date = timestamp.toDate()

                                val sdf = SimpleDateFormat("M/d/yyyy h:mm a", Locale.getDefault())
                                val formattedDate = sdf.format(date)

                                val newTopUp = mapOf(
                                    "dateTime" to formattedDate,
                                    "tax" to tax,
                                    "topUpAmount" to topUpAmount,
                                    "totalCost" to totalCost
                                )

                                val updateMap = mapOf(topUpId to newTopUp)

                                topUpRef.set(updateMap, SetOptions.merge())
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("Top Up", "Successful Record")
                                        } else {
                                            Log.d("Top Up", "Failure to Record")
                                        }
                                    }
                            }

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

                Toast.makeText(requireContext(), "Payment successful!", Toast.LENGTH_SHORT).show()
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Canceled -> {
                Toast.makeText(requireContext(), "Payment canceled", Toast.LENGTH_SHORT).show()
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Failed -> {
                Toast.makeText(requireContext(), "Payment failed: ${paymentResult.error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

}