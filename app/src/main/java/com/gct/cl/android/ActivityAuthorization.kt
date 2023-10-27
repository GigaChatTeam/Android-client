package com.gct.cl.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import com.jsoniter.spi.JsonException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.util.InternalAPI
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


@OptIn(InternalAPI::class)
class ActivityAuthorization : AppCompatActivity() {
    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
        }
    }
    private var blocked = false

    private val path by lazy { if (DEBUG) getExternalFilesDir("") else filesDir }
    private val tokensFile by lazy { File(path, "localAccounts.sjson").apply { createNewFile() } }

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
        inputLogin = findViewById(R.id.activityAuthorization_widgets_inputLogin)
        inputPassword = findViewById(R.id.activityAuthorization_widgets_inputPassword)
        buttonLogIn = findViewById(R.id.activityAuthorization_widgets_login)

        showPassword = findViewById(R.id.activityAuthorization_widgets_showPassword)
        responseStatusView = findViewById(R.id.activityAuthorization_widgets_responseStatus)

        buttonLogIn.setOnClickListener {
            if (blocked) {
                tooManyRequests()
                return@setOnClickListener
            } else blocked = true

            responseStatusView.text = ""

            GlobalScope.launch(Dispatchers.IO) {
                val response: HttpResponse =
                    httpClient.request(
                        URLS.AUTHORIZATION
                    ) {
                        method = HttpMethod.Post
                        url {
                            parameters.append(
                                URLS.AUTHORIZATION_USERNAME,
                                inputLogin.text.toString()
                            )
                        }
                        body = MultiPartFormDataContent(formData {
                            append(URLS.AUTHORIZATION_USERNAME, inputLogin.text.toString())
                            append(URLS.AUTHORIZATION_PASSWORD, inputPassword.text.toString())
                        })
                    }

                Log.d("HTTP-STATUS", response.status.value.toString())
                Log.d("HTTP-RESPONSE", response.bodyAsText())

                when (response.status.value) {
                    200 -> completeAuthorization(response.bodyAsText())
                    404 -> userNotFound()
                    406 -> outdatedClient()
                    429 -> tooManyRequests()
                    else -> unprocessedResponse()
                }

                blocked = false
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
            runOnUiThread {
                responseData?.apply {
                    Log.d(
                        "AUTH-DATA",
                        "id: ${responseData.data.id}\ntoken: ${responseData.data.token}"
                    )
                } ?: unprocessedResponse()
            }

            Thread {
                saveToken(responseData.data.id, responseData.data.token)
            }.start()

            startActivity(Intent(this, ActivityMain::class.java).apply {
                putExtra("id", responseData.data.id)
                putExtra("token", responseData.data.token)
            })
        } catch (_: NullPointerException) {
            unprocessedResponse()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun userNotFound() {
        runOnUiThread {
            responseStatusView.text =
                getString(R.string.authorization_widgets_responseStatus_userNotFound)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun unprocessedResponse() {
        runOnUiThread {
            responseStatusView.text =
                getString(R.string.authorization_widgets_responseStatus_responseError)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun outdatedClient() {
        runOnUiThread {
            responseStatusView.text =
                getString(R.string.authorization_widgets_responseStatus_outdatedClient)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun tooManyRequests() {
        runOnUiThread {
            responseStatusView.text =
                getString(R.string.authorization_widgets_responseStatus_tooManyRequests)
        }
    }

    private fun saveToken(id: Long, token: String) {
        val accounts = mutableListOf<Helper.LocalAccount>()

        for (data in tokensFile.readText().split(";")) {
            try {
                accounts.add(JsonIterator.deserialize(data, Helper.LocalAccount::class.java) .apply { main = false })
            } catch (_: JsonException) { continue }

            Log.d("TOKEN TO SAVE", JsonStream.serialize(JsonIterator.deserialize(data, Helper.LocalAccount::class.java)))
        }

        accounts.add(Helper.constructLocalAccount(token, id))

        val writableAccounts = mutableListOf<String>()

        for (account in accounts) {
            writableAccounts.add(JsonStream.serialize(account))
        }

        tokensFile.writeText(writableAccounts.joinToString(";"))
    }
}