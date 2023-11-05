package com.gct.cl.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod


class ActivityRegister : AppCompatActivity() {
    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    private fun binder() {

    }
}