package com.example.nammasantheledger10.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE customerName LIKE :searchQuery ORDER BY timestamp DESC")
    fun searchTransactions(searchQuery: String): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE isCredit = 1")
    fun getTotalCredit(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isCredit = 0")
    fun getTotalPayment(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isCredit = 1 AND timestamp >= :startOfDay")
    fun getDailySales(startOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isCredit = 0 AND timestamp >= :startOfDay")
    fun getDailyCollections(startOfDay: Long): Flow<Double?>

    @Delete
    suspend fun delete(transaction: Transaction)
}
