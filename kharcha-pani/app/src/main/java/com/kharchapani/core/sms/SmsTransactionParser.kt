package com.kharchapani.core.sms

import com.kharchapani.core.model.ParsedTransaction
import com.kharchapani.core.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

class SmsTransactionParser {

    private val federalPattern = Regex(
        pattern = """Rs\s*([\d,]+(?:\.\d{1,2})?)\s+(sent|received)\s+via\s+UPI\s+on\s+(\d{2}-\d{2}-\d{4})\s+at\s+(\d{2}:\d{2}:\d{2})\s+to\s+(.+?)\.Ref[:\s]*(\w+)""",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    private val sbiPattern = Regex(
        pattern = """A/C\s+\w+\s+(debited|credited)\s+by\s+([\d,]+(?:\.\d{1,2})?)\s+on\s+date\s+(\d{2}[A-Za-z]{3}\d{2})\s+trf\s+to\s+(.+?)\s+Refno\s+(\w+)""",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    private val otpOrPromotionalTerms = listOf(
        " otp ", "one time password", "cashback", "offer", "loan", "credit card apply", "reward points"
    )

    fun parse(message: String): ParsedTransaction? {
        if (!isRelevantBankingMessage(message)) return null

        return parseFederal(message)
            ?: parseSbi(message)
    }

    private fun parseFederal(message: String): ParsedTransaction? {
        val match = federalPattern.find(message) ?: return null

        val amount = match.groupValues[1].toMoney()
        val action = match.groupValues[2].lowercase(Locale.ENGLISH)
        val type = if (action == "sent") TransactionType.DEBIT else TransactionType.CREDIT
        val date = parseDate(match.groupValues[3], "dd-MM-uuuu")
        val time = parseTime(match.groupValues[4], "HH:mm:ss")

        return ParsedTransaction(
            amount = amount,
            type = type,
            vendor = normalizeVendor(match.groupValues[5]),
            upiId = extractUpiId(message),
            transactionRef = match.groupValues[6],
            occurredAt = if (date != null && time != null) LocalDateTime.of(date, time) else null,
            remainingBalance = extractBalance(message),
            rawMessage = message
        )
    }

    private fun parseSbi(message: String): ParsedTransaction? {
        val match = sbiPattern.find(message) ?: return null

        val action = match.groupValues[1].lowercase(Locale.ENGLISH)
        val type = if (action == "debited") TransactionType.DEBIT else TransactionType.CREDIT

        return ParsedTransaction(
            amount = match.groupValues[2].toMoney(),
            type = type,
            vendor = normalizeVendor(match.groupValues[4]),
            upiId = extractUpiId(message),
            transactionRef = match.groupValues[5],
            occurredAt = parseDate(match.groupValues[3], "ddMMMyy")?.atStartOfDay(),
            remainingBalance = extractBalance(message),
            rawMessage = message
        )
    }

    private fun isRelevantBankingMessage(message: String): Boolean {
        val text = " ${message.lowercase(Locale.ENGLISH)} "

        val looksTransactional = text.contains(" debited ") || text.contains(" credited ") || text.contains(" sent via upi ")
        if (!looksTransactional) return false

        val hasIgnoreTerms = otpOrPromotionalTerms.any { term -> text.contains(term) }
        return !hasIgnoreTerms
    }

    private fun extractUpiId(message: String): String? {
        val upiRegex = Regex("""([\w.\-]{2,})@([\w]{2,})""")
        return upiRegex.find(message)?.value
    }

    private fun extractBalance(message: String): BigDecimal? {
        val balRegex = Regex("""(?:bal(?:ance)?\s*(?:is|:)?\s*|avl\s*bal\s*[:]?\s*)([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
        return balRegex.find(message)?.groupValues?.get(1)?.toMoney()
    }

    private fun normalizeVendor(vendorRaw: String?): String? {
        return vendorRaw
            ?.trim()
            ?.replace("\\s+".toRegex(), " ")
            ?.uppercase(Locale.ENGLISH)
    }

    private fun parseDate(input: String, pattern: String): LocalDate? {
        val formatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern(pattern)
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.SMART)
        return try {
            LocalDate.parse(input.trim(), formatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun parseTime(input: String, pattern: String): LocalTime? {
        return try {
            LocalTime.parse(input.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun String.toMoney(): BigDecimal =
        replace(",", "").trim().toBigDecimal()
}
