package com.example.nammasantheledger10

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammasantheledger10.data.Transaction
import com.example.nammasantheledger10.data.TransactionViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupDashboard()
        setupSearch()

        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvTransactions)
        adapter = TransactionAdapter { transaction ->
            sendWhatsAppReminder(transaction)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        lifecycleScope.launch {
            viewModel.transactions.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    private fun setupDashboard() {
        viewModel.totalOutstanding.observe(this) { total ->
            findViewById<TextView>(R.id.tvTotalOutstanding).text = "₹ ${String.format("%.2f", total)}"
        }
        viewModel.dailyTotalCredit.observe(this) { total ->
            findViewById<TextView>(R.id.tvDailySales).text = "₹ ${total.toInt()}"
        }
        viewModel.dailyTotalPayment.observe(this) { total ->
            findViewById<TextView>(R.id.tvDailyPaid).text = "₹ ${total.toInt()}"
        }
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showAddTransactionDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val etName = view.findViewById<EditText>(R.id.etCustomerName)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val rgType = view.findViewById<RadioGroup>(R.id.rgType)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val isCredit = rgType.checkedRadioButtonId == R.id.rbUdari

            if (name.isNotEmpty() && amount > 0) {
                viewModel.addTransaction(name, amount, isCredit)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun sendWhatsAppReminder(transaction: Transaction) {
        if (!transaction.isCredit) return // Only for dues
        
        val message = "Hi ${transaction.customerName}, this is a friendly reminder of your due amount: ₹${transaction.amount}. Please clear it soon. Thank you!"
        val url = "https://api.whatsapp.com/send?text=${URLEncoder.encode(message, "UTF-8")}"
        
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}
