package com.example.nammasantheledger10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nammasantheledger10.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val onWhatsAppClick: (Transaction) -> Unit) :
    ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val btnWhatsApp: ImageButton = view.findViewById(R.id.btnWhatsApp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.tvName.text = transaction.customerName
        
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(transaction.timestamp))
        
        val prefix = if (transaction.isCredit) "+" else "-"
        holder.tvAmount.text = "$prefix ₹${String.format("%.2f", transaction.amount)}"
        holder.tvAmount.setTextColor(
            if (transaction.isCredit) 0xFF2E7D32.toInt() else 0xFF1976D2.toInt()
        )

        holder.btnWhatsApp.setOnClickListener { onWhatsAppClick(transaction) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem == newItem
    }
}
