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
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.android.material.textfield.TextInputEditText
import com.jsoniter.JsonIterator
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

    @Suppress("DEPRECATION")
    private val mainKeyAlias by lazy {
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    @Suppress("DEPRECATION")
    private val tokensFile by lazy {
        val fileToWrite = File(filesDir, "tokens.json")

        EncryptedFile.Builder(
            fileToWrite,
            application,
            mainKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

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
                            append("username", inputLogin.text.toString())
                            append("password", inputPassword.text.toString())
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

    }
}