package com.kharchapani.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KharchaPaniApp(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showManualEntry by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kharcha Pani") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showManualEntry = !showManualEntry }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(todaySpend = state.todaySpend)

            Text("Categories", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.categories.take(4).forEach { category ->
                    AssistChip(
                        onClick = { viewModel.onCategorySelected(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            if (showManualEntry) {
                ManualEntryCard(
                    onSave = { amount, vendor, note ->
                        viewModel.addManualExpense(amount, vendor, note)
                        showManualEntry = false
                    }
                )
            }

            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.transactions) { item ->
                    TransactionRow(item)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(todaySpend: Double) {
    val rupeeFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today Spend", style = MaterialTheme.typography.labelLarge)
            Text(
                text = rupeeFormat.format(todaySpend),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ManualEntryCard(
    onSave: (amount: Double, vendor: String, note: String) -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    var vendor by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Manual Entry", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = vendor,
                onValueChange = { vendor = it },
                label = { Text("Vendor") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: return@TextButton
                        if (amount > 0) onSave(amount, vendor, note)
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(item: com.kharchapani.app.domain.TransactionItem) {
    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH) }
    val amountColor = if (item.isDebit) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val amountPrefix = if (item.isDebit) "-" else "+"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.vendor, fontWeight = FontWeight.SemiBold)
                Text(item.categoryName, style = MaterialTheme.typography.labelMedium)
                Text(
                    sdf.format(Date(item.occurredAtEpochMillis)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "$amountPrefix₹${"%.2f".format(item.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
