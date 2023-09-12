package com.gct.cl.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActivityAuthorization : AppCompatActivity() {
    val httpClient: HttpClient = HttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)
    }

    private fun setupWidgetsEvents() {

    }

    private object Requests {
        @OptIn(DelicateCoroutinesApi::class)
        private fun getToken(login: String, password: String) {
            GlobalScope.launch(Dispatchers.IO) {
                
            }
        }
    }
}