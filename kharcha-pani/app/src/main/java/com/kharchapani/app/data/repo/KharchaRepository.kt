package com.kharchapani.app.data.repo

import com.kharchapani.app.data.local.CategoryEntity
import com.kharchapani.app.data.local.KharchaDao
import com.kharchapani.app.data.local.TransactionEntity
import com.kharchapani.app.domain.Category
import com.kharchapani.app.domain.TransactionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KharchaRepository(private val dao: KharchaDao) {

    fun observeCategories(): Flow<List<Category>> =
        dao.observeCategories().map { list -> list.map { Category(it.id, it.name) } }

    fun observeTransactions(): Flow<List<TransactionItem>> =
        dao.observeTransactions().map { rows ->
            rows.map {
                TransactionItem(
                    id = it.transaction.id,
                    amount = it.transaction.amount,
                    isDebit = it.transaction.isDebit,
                    vendor = it.transaction.vendor,
                    categoryName = it.category.name,
                    occurredAtEpochMillis = it.transaction.occurredAtEpochMillis,
                )
            }
        }

    fun observeTodaySpend(startInclusive: Long, endInclusive: Long): Flow<Double> =
        dao.observeSpendBetween(startInclusive, endInclusive)

    suspend fun addManualTransaction(
        amount: Double,
        isDebit: Boolean,
        vendor: String,
        categoryId: Long,
        timestampMillis: Long,
        note: String? = null,
    ) {
        dao.insertTransaction(
            TransactionEntity(
                amount = amount,
                isDebit = isDebit,
                vendor = vendor,
                categoryId = categoryId,
                occurredAtEpochMillis = timestampMillis,
                note = note,
                source = "MANUAL",
            )
        )
    }

    suspend fun seedDefaultCategoriesIfNeeded() {
        if (dao.categoryCount() > 0) return

        val defaults = listOf(
            "Food & Dining",
            "Transport",
            "Shopping",
            "Bills & Utilities",
            "Entertainment",
            "Health",
            "Subscriptions/EMIs",
            "Transfers",
            "Rent",
            "Education",
            "Other"
        )

        defaults.forEach { dao.insertCategory(CategoryEntity(name = it, isDefault = true)) }
    }
}
