package com.kharchapani.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface KharchaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY occurredAtEpochMillis DESC")
    fun observeTransactions(): Flow<List<TransactionWithCategory>>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isDebit = 1 AND occurredAtEpochMillis BETWEEN :startInclusive AND :endInclusive"
    )
    fun observeSpendBetween(startInclusive: Long, endInclusive: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun categoryCount(): Int
}
