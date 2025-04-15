package com.example.gofare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactionList: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transactionId: TextView = itemView.findViewById(R.id.transactionId)
        private val balance: TextView = itemView.findViewById(R.id.balance)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val pickUpTxt: TextView = itemView.findViewById(R.id.pickUp)
        private val dropOffTxt: TextView = itemView.findViewById(R.id.dropOff)
        private val total: TextView = itemView.findViewById(R.id.total)

        fun bind(transaction: Transaction) {
            transactionId.text = transaction.transactionId ?: "No ID"
            balance.text = "Balance: ${transaction.currentBalance}"
            date.text = "Date: ${transaction.dateTime}"
            pickUpTxt.text = "Pick Up: ${transaction.pickup}"
            dropOffTxt.text = "Drop Off: ${transaction.dropoff}"
            total.text = "Total: ${transaction.totalAmount}"

            itemView.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactionList[position])
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactionList = newTransactions
        notifyDataSetChanged()
    }
}
