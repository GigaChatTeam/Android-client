package com.gct.cl.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.jsoniter.JsonIterator
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ActivityAuthorization : AppCompatActivity() {
    private val httpClient = HttpClient()

    private lateinit var debugView: TextView

    private lateinit var inputLogin: TextInputEditText
    private lateinit var inputPassword: TextInputEditText
    private lateinit var buttonLogIn: Button
    private lateinit var showPassword: CheckBox
    private lateinit var responseStatusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        supportActionBar?.hide()

        binder()
    }

    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    private fun binder() {
        debugView = findViewById(R.id.debugView)

        inputLogin = findViewById(R.id.activityAuthorization_widgets_inputLogin)
        inputPassword = findViewById(R.id.activityAuthorization_widgets_inputPassword)
        buttonLogIn = findViewById(R.id.activityAuthorization_widgets_login)

        showPassword = findViewById(R.id.activityAuthorization_widgets_showPassword)
        responseStatusView = findViewById(R.id.activityAuthorization_widgets_responseStatus)

        buttonLogIn.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val response: HttpResponse =
                    httpClient.request(
                        URLS.AUTHORIZATION
                    ) {
                        method = HttpMethod.Get
                        url {
                            parameters.append(
                                URLS.AUTHORIZATION_USERNAME,
                                inputLogin.text.toString()
                            )
                            parameters.append(
                                URLS.AUTHORIZATION_PASSWORD,
                                inputPassword.text.toString()
                            )
                        }
                    }

                when (response.status.value) {
                    200 -> completeAuthorization(response.bodyAsText())
                    404 -> userNotFound()
                    else -> unprocessedResponse()
                }
            }
        }

        showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) inputPassword.setInputType(InputType.TYPE_CLASS_TEXT)
            else inputPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun completeAuthorization(responseText: String) {
        val responseData = JsonIterator.deserialize(
            responseText,
            ResponsePackets.AC.Authorization.Done::class.java
        )

        try {
            responseData?.apply {
                debugView.text = "id: ${
                    responseData.data.id
                }\ntoken: ${responseData.data.token}"
            } ?: unprocessedResponse()
        } catch (_: NullPointerException) { unprocessedResponse() }
    }

    @SuppressLint("SetTextI18n")
    private fun userNotFound() {
        responseStatusView.text = "123"
    }

    @SuppressLint("SetTextI18n")
    private fun unprocessedResponse() {
        responseStatusView.text = "123"
    }
}