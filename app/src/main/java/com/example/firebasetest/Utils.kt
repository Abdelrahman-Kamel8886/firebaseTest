package com.example.firebasetest

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity

fun Context.getSimsCount(): String {
    val subscriptionManager = this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
        return "Permission not granted. Please allow phone permissions."
    }

    return try {
        val activeSubs: List<SubscriptionInfo>? = subscriptionManager.activeSubscriptionInfoList
        val count = activeSubs?.size ?: 0
        "Active SIM Count: $count"
    } catch (e: SecurityException) {
        "Permission error: ${e.message}"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun Context.getSimsOperators(): String {

    val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val subscriptionManager = this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
        return "Permission not granted. Please allow phone permissions."
    }

    return try {
        val activeSubs: List<SubscriptionInfo>? = subscriptionManager.activeSubscriptionInfoList
        if (activeSubs.isNullOrEmpty()) {
            return "No active SIMs detected."
        }
        var result = ""
        var index = 1

        for (subInfo in activeSubs) {
            val tmForSim = telephonyManager.createForSubscriptionId(subInfo.subscriptionId)

            val carrier = subInfo.carrierName ?: "Unknown"
            val operator = tmForSim.networkOperatorName ?: "Unknown"
            result += "SIM ${index++}: Carrier: $carrier | Operator: $operator \n"
        }

        result

    } catch (e: SecurityException) {
        "Permission error: ${e.message}"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun Context.getPhoneNumberHintIntent(
    onSuccess: (IntentSender) -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        val request = GetPhoneNumberHintIntentRequest.builder().build()
        val signInClient = Identity.getSignInClient(this)

        signInClient.getPhoneNumberHintIntent(request)
            .addOnSuccessListener { result ->
                onSuccess(result.intentSender)
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Error creating phone number hint intent: ${e.message}")
                onFailure("Phone number hint not available: ${e.message}")
            }
    } catch (e: Exception) {
        Log.e("TAG", "Error creating phone number hint intent: ${e.message}")
        onFailure("Error: ${e.message}")
    }
}

fun Context.getSimSlotFromSubscriptionId(subscriptionId: Int): String {
    if (subscriptionId == -1) return "Unknown SIM"

    val subscriptionManager = this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        ?: return "Subscription manager not available"

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        return "Permission not granted"
    }

    return try {
        val info = subscriptionManager.getActiveSubscriptionInfo(subscriptionId)
        val carrier = info?.carrierName ?: "Unknown"
        val slot = info?.simSlotIndex ?: -1
        "SIM ${slot + 1}: $carrier"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

