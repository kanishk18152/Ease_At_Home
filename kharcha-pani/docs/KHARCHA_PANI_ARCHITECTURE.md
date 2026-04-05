# Kharcha Pani — Native Android Architecture Blueprint

This document translates the product brief into an implementable Android architecture using **Kotlin + Jetpack Compose + Material 3 + Room**, with an **offline-first** approach.

## 1) Tech Stack

- **UI**: Jetpack Compose (Material 3 + dynamic color)
- **Architecture**: MVVM + Repository + Use Cases
- **Storage**: Room (SQLite), DataStore (preferences/settings)
- **Background work**: WorkManager (bulk SMS import, recurring detection)
- **Dependency Injection**: Hilt
- **Charts**: Compose-based chart library (e.g., Vico) or custom Canvas charts
- **Export**: CSV via file writer, PDF via Android PdfDocument

## 2) App Modules (suggested)

- `app`
- `core-model` (entities, DTOs, enums)
- `core-data` (Room, DAOs, repositories)
- `core-sms` (parsers + sender filtering)
- `feature-dashboard`
- `feature-transactions`
- `feature-budget`
- `feature-recurring`
- `feature-settings`

## 3) Data Model (Room)

### `transactions`
- `id: Long` (PK)
- `source: TransactionSource` (SMS_AUTO, SMS_IMPORT, MANUAL)
- `type: TransactionType` (DEBIT, CREDIT)
- `amount: BigDecimal`
- `vendorName: String?`
- `upiId: String?`
- `txnRef: String?`
- `smsSender: String?`
- `occurredAt: Instant`
- `categoryId: Long`
- `isRecurring: Boolean`
- `note: String?`
- `paymentMethod: PaymentMethod` (UPI, CASH, CARD, OTHER)
- `rawSmsId: Long?` (for dedupe/tracing)

Indexes:
- `(occurredAt)`
- `(categoryId, occurredAt)`
- unique on `(txnRef, amount, occurredAt)` where possible

### `categories`
- `id: Long` (PK)
- `name: String` (unique)
- `iconName: String`
- `colorSeed: Long?`
- `isDefault: Boolean`
- `isDeleted: Boolean`

### `vendor_category_map`
- `id: Long`
- `vendorKey: String` (normalized)
- `categoryId: Long`
- `confidence: Float`
- `updatedAt: Instant`

### `budgets`
- `id: Long`
- `monthKey: String` (e.g., `2026-04`)
- `overallLimit: BigDecimal?`

### `category_budgets`
- `id: Long`
- `monthKey: String`
- `categoryId: Long`
- `limit: BigDecimal`

### `sms_import_state`
- `id: Int = 1`
- `lastImportedSmsTimestamp: Long`
- `firstRunCompleted: Boolean`

## 4) SMS Parsing Strategy

1. Filter sender IDs using allow-list hints (banks/UPI senders) + pattern checks.
2. Reject OTP/promotional messages (`OTP`, `offer`, `cashback`, etc.) before heavy parsing.
3. Attempt parser templates in priority order (bank-specific regex pack).
4. Normalize parsed vendor names and categories.
5. Dedupe by txnRef + amount + near-time window.
6. Persist and emit UI updates.

## 5) Real-Time + Bulk Import Flow

- **Real-time**: `BroadcastReceiver` for `SMS_RECEIVED` -> parser -> repository save.
- **First launch import**: WorkManager one-time task scans inbox (READ_SMS), parses historical messages, inserts in batches.
- **Permission UX**:
  - Explain why SMS permission is needed.
  - If denied, app still works with manual entry.

## 6) Dashboard Views

- **Home**: today total, week/month quick card, today's list.
- **Daily**: date picker + transactions.
- **Weekly**: bars (Mon-Sun), total, category split.
- **Monthly**: trend line, donut/pie, top merchants, prev-month delta.
- **Yearly**: month bars, annual total, category distribution, monthly avg.

## 7) Auto Categorization

Priority:
1. user-learned `vendor_category_map`
2. built-in dictionary (Swiggy, Zomato, Uber, Ola, etc.)
3. keyword fallback
4. `Other`

When user edits category manually:
- update transaction
- upsert vendor mapping to improve future predictions

## 8) Recurring Detection

Nightly WorkManager task:
- group by normalized vendor + rounded amount
- detect periodicity near 28–31 days (or fixed intervals)
- flag recurring and show in dedicated screen

## 9) Export + Backup

- CSV export: transaction rows + filters.
- PDF export: summary + category + charts snapshot.
- DB backup/restore: copy Room DB to selected SAF URI and import back safely (with app restart prompt).

## 10) Security and Privacy

- No internet permission.
- Local-only data.
- Optional in-app passcode/biometric lock can be future enhancement.

## 11) Suggested Milestones

1. M1: Room schema + manual entry + category management
2. M2: SMS real-time parser + first-run import
3. M3: Dashboard daily/weekly/monthly/yearly charts
4. M4: Budget alerts + recurring detector + export/backup
5. M5: polish (animations, accessibility, dark mode tuning)
