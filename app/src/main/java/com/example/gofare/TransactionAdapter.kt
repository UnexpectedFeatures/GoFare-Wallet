package com.example.gofare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactionList: List<Transaction>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnItemClickListener {
        fun onTransactionClick(transaction: Transaction)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transactionId: TextView = itemView.findViewById(R.id.transactionId)
        private val balance: TextView = itemView.findViewById(R.id.balance)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val pickUpTxt: TextView = itemView.findViewById(R.id.pickUp)
        private val dropOffTxt: TextView = itemView.findViewById(R.id.dropOff)
        private val total: TextView = itemView.findViewById(R.id.total)

        fun bind(transaction: Transaction) {

            transactionId.text = transaction.transactionId ?: "No ID"
            balance.text = "Balance: ${transaction.balance}"
            date.text = "Date: ${transaction.date}"
            pickUpTxt.text = "Pick Up: ${transaction.pickup}"
            dropOffTxt.text = "Drop Off: ${transaction.dropoff}"
            total.text = "Total: ${transaction.total}"

            itemView.setOnClickListener {
                listener.onTransactionClick(transaction)
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