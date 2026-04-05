package com.kharchapani.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index("occurredAtEpochMillis"), Index("categoryId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val isDebit: Boolean,
    val vendor: String,
    val categoryId: Long,
    val occurredAtEpochMillis: Long,
    val note: String? = null,
    val source: String = "MANUAL",
)
