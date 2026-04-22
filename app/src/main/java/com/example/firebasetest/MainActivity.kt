package com.example.firebasetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {

    val result = remember { mutableStateOf("result will show here") }
    val context = LocalContext.current
    val activity = LocalActivity.current as Activity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val data: Intent? = res.data
            val credential = Identity.getSignInClient(activity)
                .getPhoneNumberFromIntent(data)
            result.value = credential ?: "No number selected"
        } else {
            result.value = "User canceled or no number available"
        }
    }

//    DisposableEffect(Unit) {
//        SmsReceiver.onSmsReceived = { number, sender, body ->
//            result.value = "From: $sender : $number \n $body"
//        }
//        onDispose {
//            SmsReceiver.onSmsReceived = null
//        }
//    }

/*    DisposableEffect(Unit) {
        SmsReceiver.onSmsReceived = { number, sender, body, subscriptionId ->
            val simInfo = context.getSimSlotFromSubscriptionId(subscriptionId)
            result.value = "From: $sender : $number\nReceived on: $simInfo\n$body"
        }
        onDispose {
            SmsReceiver.onSmsReceived = null
        }
    }*/

    DisposableEffect(Unit) {
        SmsReceiver.onSmsReceived = { number, sender, body, subscriptionId ->
            val simInfo = context.getSimSlotFromSubscriptionId(subscriptionId)
            result.value = "From: $sender : $number\nReceived on: $simInfo\n$body"
        }
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        val listener = object : SubscriptionManager.OnSubscriptionsChangedListener() {
            override fun onSubscriptionsChanged() {
                val activeSubs = subscriptionManager.activeSubscriptionInfoList
                val count = activeSubs?.size ?: 0
                result.value = "SIMs changed! Current active SIM count: $count"
            }
        }

        subscriptionManager.addOnSubscriptionsChangedListener(listener)
        onDispose {
            SmsReceiver.onSmsReceived = null
            subscriptionManager.removeOnSubscriptionsChangedListener(listener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

        ) {
        Button(
            onClick = {
                result.value = context.getSimsCount()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonColors(
                containerColor = Color.Green,
                contentColor = Color.Black,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.LightGray
            )
        ) {
            Text(text = "get SIMs count")
        }

        Button(
            onClick = {
                result.value = context.getSimsOperators()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonColors(
                containerColor = Color.Green,
                contentColor = Color.Black,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.LightGray
            )
        ) {
            Text(text = "get SIMs Operators")
        }

        Button(
            onClick = {
                try {
                    val request = GetPhoneNumberHintIntentRequest.builder().build()
                    val client = Identity.getSignInClient(activity)
                    client.getPhoneNumberHintIntent(request)
                        .addOnSuccessListener { pendingIntent ->
                            launcher.launch(
                                IntentSenderRequest.Builder(pendingIntent).build()
                            )
                        }
                        .addOnFailureListener {
                            result.value = "Error: ${it.message}"
                        }
                } catch (e: Exception) {
                    result.value = "Exception: ${e.message}"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "get SIMs Numbers")
        }

        Button(
            onClick = {

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonColors(
                containerColor = Color.Green,
                contentColor = Color.Black,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.LightGray
            )
        ) {
            Text(text = "sms detect sender number & name")
        }

        Button(
            onClick = {

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "sms detect receiver SIM")
        }

        Text(
            text = result.value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}