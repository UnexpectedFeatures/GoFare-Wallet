package com.example.gofare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TopUpHistoryAdapter(
    private var topUpHistoryList: List<TopUpHistory>,
    private val onItemClick: (TopUpHistory) -> Unit
) : RecyclerView.Adapter<TopUpHistoryAdapter.TopUpHistoryViewHolder>() {

    inner class TopUpHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val totalCost: TextView = itemView.findViewById(R.id.totalCost)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val total: TextView = itemView.findViewById(R.id.total)

        fun bind(topUpHistory: TopUpHistory) {
            totalCost.text = "PHP ${topUpHistory.totalAmount}"
            date.text = "Date: ${topUpHistory.dateTime}"
            total.text = "+ ${topUpHistory.topUpAmount}"

            itemView.setOnClickListener {
                onItemClick(topUpHistory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopUpHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.top_up_item, parent, false)
        return TopUpHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopUpHistoryViewHolder, position: Int) {
        holder.bind(topUpHistoryList[position])
    }

    override fun getItemCount(): Int = topUpHistoryList.size

    fun updateTopUpHistory(newTopUp: List<TopUpHistory>) {
        topUpHistoryList = newTopUp
        notifyDataSetChanged()
    }
}
