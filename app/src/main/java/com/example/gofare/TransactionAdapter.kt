package com.example.gofare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactionList: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionId: TextView = itemView.findViewById(R.id.transactionId)
        val balance: TextView = itemView.findViewById(R.id.balance)
        val date: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.transactionId.text = transaction.transactionId ?: "No ID"
        holder.balance.text = "Balance: ${transaction.balance}"
        holder.date.text = "Date: ${transaction.date}"
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    // Method to update the transactions in the adapter
    fun updateTransactions(newTransactions: List<Transaction>) {
        transactionList.clear() // Clear existing data
        transactionList.addAll(newTransactions) // Add new data
        notifyDataSetChanged() // Notify adapter about the change
    }
}
