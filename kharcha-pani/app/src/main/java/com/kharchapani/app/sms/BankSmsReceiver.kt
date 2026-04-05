package com.kharchapani.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.kharchapani.core.sms.SmsTransactionParser

class BankSmsReceiver : BroadcastReceiver() {

    private val parser = SmsTransactionParser()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        messages.forEach { sms ->
            val body = sms.messageBody ?: return@forEach
            val parsed = parser.parse(body)
            if (parsed != null) {
                Log.d("BankSmsReceiver", "Parsed txn ref=${parsed.transactionRef}, amount=${parsed.amount}")
                // TODO: persist transaction via repository in a background-safe way.
            }
        }
    }
}
