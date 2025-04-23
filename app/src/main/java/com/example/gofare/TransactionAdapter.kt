package com.example.gofare

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.gofare.NotificationHelper.sendNotification

class TransactionAdapter(
    private var transactionList: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit

) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val location: TextView = itemView.findViewById(R.id.location)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val total: TextView = itemView.findViewById(R.id.total)
        private val transactionItemBg: ConstraintLayout = itemView.findViewById(R.id.transactionItemBg)
        private val viewBar: View = itemView.findViewById(R.id.viewBar)

        fun bind(transaction: Transaction) {
            val softRed = Color.parseColor("#E57373")
            val mutedCrimson = Color.parseColor("#C94C4C")
            val white = Color.parseColor("#FFFFFF")

            location.text = transaction.pickup + " - " + transaction.dropoff
            date.text = "Date: ${transaction.dateTime}"
            total.text = "- ${transaction.totalAmount}"

            if (transaction.refunded == true){
                transactionItemBg.setBackgroundColor(softRed)
                viewBar.setBackgroundColor(mutedCrimson)
                location.setTextColor(white)
                date.setTextColor(white)
                total.setTextColor(white)
            }

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
