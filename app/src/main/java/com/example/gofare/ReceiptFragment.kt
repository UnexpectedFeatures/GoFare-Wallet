package com.example.gofare

import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class ReceiptFragment : Fragment() {

    private lateinit var transactionId : TextView
    private lateinit var pickup : TextView
    private lateinit var dropoff : TextView
    private lateinit var loaned : TextView
    private lateinit var dateTime : TextView
    private lateinit var balance : TextView
    private lateinit var total : TextView
    private lateinit var remainingBalance : TextView
    private lateinit var remBalTV : TextView
    private lateinit var refundBtn : com.google.android.material.button.MaterialButton
    private lateinit var refundedTv : TextView

    companion object {
        fun newInstance(transaction: Transaction): ReceiptFragment {
            val fragment = ReceiptFragment()
            val bundle = Bundle()
            bundle.putParcelable("selectedTransaction", transaction)
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
        val view = inflater.inflate(R.layout.fragment_receipt, container, false)

        refundBtn = view.findViewById(R.id.refundBtn)
        transactionId = view.findViewById(R.id.transactionId)
        pickup = view.findViewById(R.id.pickUp)
        dropoff = view.findViewById(R.id.dropOff)
        loaned = view.findViewById(R.id.loaned)
        dateTime = view.findViewById(R.id.dateTime)
        balance = view.findViewById(R.id.balance)
        total = view.findViewById(R.id.total)
        remainingBalance = view.findViewById(R.id.remainingBalance)
        remBalTV = view.findViewById(R.id.remBalTV)
        refundedTv = view.findViewById(R.id.refunded)

        val transaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("selectedTransaction", Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("selectedTransaction")
        }

        if (transaction == null) {
            Toast.makeText(requireContext(), "Transaction not found", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()

        Log.d("Transaction: ", transaction.toString())

        if (transaction?.loaned == false){
            remBalTV.setText("Remaining Balance:")
            loaned.visibility = View.GONE
        }
        else {
            remBalTV.setText("Loaned Amount:")
            loaned.visibility = View.VISIBLE
        }


        if (transaction?.refunded == true){
            refundedTv.visibility = View.VISIBLE
            refundBtn.visibility = View.GONE
        }

        viewModel.currency.observe(viewLifecycleOwner) { currency ->
            transactionId.text = transaction?.transactionId
            pickup.text = transaction?.pickup
            dropoff.text = transaction?.dropoff
            loaned.text = "Loaned: " + transaction?.loaned.toString()
            dateTime.text = "Date: " + transaction?.dateTime
            balance.text = "$currency " + transaction?.currentBalance.toString()
            total.text = "$currency " + transaction?.totalAmount.toString()
            if (transaction?.loaned == false){
                remainingBalance.text = "$currency " + transaction?.remainingBalance.toString()
            }
            else{
                remainingBalance.text = "$currency " + transaction?.loanedAmount.toString()
            }
        }

        refundBtn.setOnClickListener{
            showInputDialog()
        }
        return view
    }

    private fun showInputDialog() {
        val transaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("selectedTransaction", Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("selectedTransaction")
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Reason for Refund")

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
        }

        val input = EditText(requireContext()).apply {
            hint = "Enter your reason"
        }

        // Add EditText to container
        container.addView(input)

        builder.setView(container)

         builder.setPositiveButton("OK") { dialog, _ ->
            val userInput = input.text.toString()
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid

            if (userId != null && transaction?.transactionId != null) {
                val db = FirebaseFirestore.getInstance()
                val refundsRef = db.collection("Refunds").document(userId)
                val unapprovedRef = refundsRef.collection("Unapproved")
                val transactionId = transaction.transactionId

                unapprovedRef.document(transactionId).get()
                    .addOnSuccessListener { docSnapshot ->
                        if (docSnapshot.exists()) {
                            Toast.makeText(requireContext(), "This refund has already been requested.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        unapprovedRef.get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.size() >= 1) {
                                    Toast.makeText(requireContext(), "You already have an ongoing refund request.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val refundInfo = mapOf(
                                        "userId" to userId,
                                        "transactionId" to transactionId,
                                        "reason" to userInput,
                                        "dateTime" to transaction.dateTime,
                                        "pickup" to transaction.pickup,
                                        "dropoff" to transaction.dropoff,
                                        "loaned" to transaction.loaned,
                                        "loanedAmount" to transaction.loanedAmount,
                                        "remainingBalance" to transaction.remainingBalance,
                                        "currentBalance" to transaction.currentBalance,
                                        "status" to "Unapproved",
                                        "totalAmount" to transaction.totalAmount,
                                        "vehicle" to "",
                                        "requestedAt" to Timestamp.now(),
                                        "originalTransactionId" to transactionId,
                                        "approvedAt" to "",
                                        "originalDocumentPath" to "",
                                        "paymentStatus" to "",
                                    )

                                    unapprovedRef.document(transactionId)
                                        .set(refundInfo)
                                        .addOnSuccessListener {
                                            Log.d("Refund", "Refund request submitted with transactionId as document ID.")
                                            Toast.makeText(requireContext(), "Refund request submitted.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Refund", "Error submitting refund request", e)
                                            Toast.makeText(requireContext(), "Failed to submit refund.", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Refund", "Error checking for existing refund", e)
                        Toast.makeText(requireContext(), "Failed to check refund status.", Toast.LENGTH_SHORT).show()
                    }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

}