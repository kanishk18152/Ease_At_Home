package com.kharchapani.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

enum class TransactionType { DEBIT, CREDIT }

data class ParsedTransaction(
    val amount: BigDecimal,
    val type: TransactionType,
    val vendor: String?,
    val upiId: String?,
    val transactionRef: String?,
    val occurredAt: LocalDateTime?,
    val remainingBalance: BigDecimal?,
    val rawMessage: String,
)
