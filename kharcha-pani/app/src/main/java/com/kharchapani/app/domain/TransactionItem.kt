package com.kharchapani.app.domain

data class TransactionItem(
    val id: Long,
    val amount: Double,
    val isDebit: Boolean,
    val vendor: String,
    val categoryName: String,
    val occurredAtEpochMillis: Long,
)
