package com.example.nammasantheledger10.data

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).transactionDao()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) dao.getAllTransactions()
            else dao.searchTransactions("%$query%")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOutstanding: LiveData<Double> = combine(
        dao.getTotalCredit().map { it ?: 0.0 },
        dao.getTotalPayment().map { it ?: 0.0 }
    ) { credit, payment -> credit - payment }.asLiveData()

    private val startOfDay: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    val dailyTotalCredit: LiveData<Double> = dao.getDailySales(startOfDay)
        .map { it ?: 0.0 }
        .asLiveData()

    val dailyTotalPayment: LiveData<Double> = dao.getDailyCollections(startOfDay)
        .map { it ?: 0.0 }
        .asLiveData()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTransaction(name: String, amount: Double, isCredit: Boolean) {
        viewModelScope.launch {
            dao.insert(Transaction(customerName = name, amount = amount, isCredit = isCredit, timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.delete(transaction)
        }
    }
}
