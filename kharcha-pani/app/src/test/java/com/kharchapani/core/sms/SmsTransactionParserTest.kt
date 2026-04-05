package com.kharchapani.core.sms

import com.kharchapani.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SmsTransactionParserTest {

    private val parser = SmsTransactionParser()

    @Test
    fun `parses federal bank debit sample`() {
        val message = "Rs 970.00 sent via UPI on 04-04-2026 at 08:25:35 to DMRC.Ref:609481394268.Not you? Call 18004251199/SMS BLOCKUPI to 98950 88888 -Federal Bank"

        val txn = parser.parse(message)

        assertNotNull(txn)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals("970.00", txn.amount.toPlainString())
        assertEquals("DMRC", txn.vendor)
        assertEquals("609481394268", txn.transactionRef)
    }

    @Test
    fun `parses sbi debit sample`() {
        val message = "Dear UPI user A/C X7125 debited by 226.20 on date 08Mar26 trf to ZOMATO LIMITED Refno 612802860676 If not u? call-1800111109 for other services-18001234-SBI"

        val txn = parser.parse(message)

        assertNotNull(txn)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals("226.20", txn.amount.toPlainString())
        assertEquals("ZOMATO LIMITED", txn.vendor)
        assertEquals("612802860676", txn.transactionRef)
    }

    @Test
    fun `ignores otp promotional messages`() {
        val message = "Your OTP is 123456 for login. Do not share with anyone."
        assertNull(parser.parse(message))
    }
}
