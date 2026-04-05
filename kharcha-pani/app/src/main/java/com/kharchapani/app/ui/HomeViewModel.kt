package com.kharchapani.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kharchapani.app.data.repo.KharchaRepository
import com.kharchapani.app.domain.Category
import com.kharchapani.app.domain.TransactionItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val transactions: List<TransactionItem> = emptyList(),
    val todaySpend: Double = 0.0,
    val selectedCategoryId: Long? = null,
)

class HomeViewModel(
    private val repository: KharchaRepository,
) : ViewModel() {

    private val selectedCategoryId = MutableStateFlow<Long?>(null)

    private val startOfTodayMillis: Long
        get() = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private val endOfTodayMillis: Long
        get() = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1).toEpochMilli()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeCategories(),
        repository.observeTransactions(),
        repository.observeTodaySpend(startOfTodayMillis, endOfTodayMillis),
        selectedCategoryId,
    ) { categories, transactions, todaySpend, selectedId ->
        HomeUiState(
            categories = categories,
            transactions = transactions,
            todaySpend = todaySpend,
            selectedCategoryId = selectedId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch {
            repository.seedDefaultCategoriesIfNeeded()
        }
    }

    fun addManualExpense(amount: Double, vendor: String, note: String?) {
        val categoryId = selectedCategoryId.value ?: return
        viewModelScope.launch {
            repository.addManualTransaction(
                amount = amount,
                isDebit = true,
                vendor = vendor.ifBlank { "Manual Entry" },
                categoryId = categoryId,
                timestampMillis = Instant.now().toEpochMilli(),
                note = note,
            )
        }
    }

    fun onCategorySelected(categoryId: Long) {
        selectedCategoryId.value = categoryId
    }

    class Factory(
        private val repository: KharchaRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
