package com.example.firebasetest

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
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

    val phoneNumberLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val phoneNumber = activityResult.data?.getStringExtra("phone_number")
            result.value = phoneNumber ?: "No phone number selected"
        } else {
            result.value = "Phone number selection cancelled"
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

    DisposableEffect(Unit) {
        SmsReceiver.onSmsReceived = { number, sender, body, subscriptionId ->
            val simInfo = context.getSimSlotFromSubscriptionId(subscriptionId)
            result.value = "From: $sender : $number\nReceived on: $simInfo\n$body"
        }
        onDispose {
            SmsReceiver.onSmsReceived = null
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
                context.getPhoneNumberHintIntent(
                    onSuccess = { intentSender ->
                        val request = IntentSenderRequest.Builder(intentSender).build()
                        phoneNumberLauncher.launch(request)
                    },
                    onFailure = { errorMessage ->
                        result.value = errorMessage
                    }
                )
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