package com.example.nammasantheledger10.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerName: String,
    val amount: Double,
    val isCredit: Boolean, 
    val timestamp: Long
)
