package com.example.firebasetest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    companion object {
        var onSmsReceived: ((String, String, String, Int) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("TAG", "onReceive: ")
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val subscriptionId = intent.getIntExtra("subscription", -1)

            for (msg in messages) {
                val number = msg.displayOriginatingAddress
                val text = msg.displayMessageBody
                val name = getContactName(context, number) ?: number
                onSmsReceived?.invoke(number, name, text, subscriptionId)
                Log.i("TAG", "onReceive: event: $name : $number : $text : SIM=$subscriptionId")
            }
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null, null, null
        ).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }
}