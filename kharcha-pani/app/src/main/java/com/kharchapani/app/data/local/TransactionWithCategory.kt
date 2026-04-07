package com.kharchapani.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
)
