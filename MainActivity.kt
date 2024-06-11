package com.example.asystentdomu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.asystentdomu.ui.theme.AsystentDomuTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isAlert = intent.getBooleanExtra("alert", false)

        setContent {
            AsystentDomuTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeSecurityStatus(isAlert, modifier = Modifier.padding(innerPadding))
                }
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("sensors")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Subscribed to sensors topic")
                }
            }
    }
}

@Composable
fun HomeSecurityStatus(isAlert: Boolean, modifier: Modifier = Modifier) {
    val statusMessage = remember { mutableStateOf("Sprawdzanie statusu...") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val status = checkDB()
            statusMessage.value = if (status) {
                "Twój dom jest bezpieczny.\nCzujniki działają prawidłowo."
            } else {
                "!!!NASTĄPIŁO WŁAMANIE!!!\nZADZWOŃ NA POLICJE"
            }
        }
    }

    if (isAlert) {
        statusMessage.value = "!!!NASTĄPIŁO WŁAMANIE!!!\nZADZWOŃ NA POLICJE"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFE0BD)) // Kolor tła cielisty
            .padding(16.dp)
    ) {
        Text(
            text = statusMessage.value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
    }
}

fun checkDB(): Boolean {
    return try {
        val url = URL("https://mim-pk-2024.ew.r.appspot.com/checkdb")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val isOkay = JSONObject(responseBody).getInt("isOkay")
            isOkay == 1
        } else {
            println("Error: HTTP response code: $responseCode")
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Preview(showBackground = true)
@Composable
fun HomeSecurityStatusPreview() {
    AsystentDomuTheme {
        HomeSecurityStatus(isAlert = false)
    }
}