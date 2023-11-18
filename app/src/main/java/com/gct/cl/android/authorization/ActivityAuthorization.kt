package com.gct.cl.android.authorization

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gct.cl.android.DEBUG
import com.gct.cl.android.R
import com.gct.cl.android.URLS
import com.gct.cl.android.main.ActivityMain
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


class ActivityAuthorization : AppCompatActivity() {
    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
        }
    }
    private var blocked = false

    private val path by lazy { if (DEBUG) getExternalFilesDir("") else filesDir }
    private val tokensFile by lazy { File(path, "localAccounts.sjson").apply { createNewFile() } }

    private val inputLogin: TextInputEditText by lazy { findViewById(R.id.activityAuthorization_widgets_inputLogin) }
    private val inputPassword: TextInputEditText by lazy { findViewById(R.id.activityAuthorization_widgets_inputPassword) }
    private val showPassword: CheckBox by lazy { findViewById(R.id.activityAuthorization_widgets_showPassword) }
    private val responseStatusView: TextView by lazy { findViewById(R.id.activityAuthorization_widgets_responseStatus) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        binder()

        supportActionBar?.hide()
    }

    private fun binder() {
        showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) inputPassword.setInputType(InputType.TYPE_CLASS_TEXT)
            else inputPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        }
    }

    @InternalAPI
    @OptIn(DelicateCoroutinesApi::class)
    fun authorize(view: View) {
        if (blocked) {
            tooManyRequests()
        } else {
            blocked = true
        }

        responseStatusView.text = ""

        GlobalScope.launch(Dispatchers.IO) {
            val response: HttpResponse = httpClient.request(
                URLS.AUTHORIZATION
            ) {
                method = HttpMethod.Post
                url {
                    parameters.append(
                        URLS.AUTHORIZATION_USERNAME, inputLogin.text.toString()
                    )
                }
                body = MultiPartFormDataContent(formData {
                    append(URLS.AUTHORIZATION_USERNAME, inputLogin.text.toString())
                    append(URLS.AUTHORIZATION_PASSWORD, inputPassword.text.toString())
                })
            }

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


    fun register(view: View) {
        startActivity(Intent(this, ActivityRegistration::class.java))
    }


    @SuppressLint("SetTextI18n")
    private fun completeAuthorization(responseText: String) {
        val responseData: Packets.Authorizations.Done

        try {
            responseData = JsonIterator.deserialize(
                responseText, Packets.Authorizations.Done::class.java
            )

            saveToken(responseData.data.id, responseData.data.token)

            startActivity(Intent(this, ActivityMain::class.java).apply {
                putExtra("id", responseData.data.id)
                putExtra("token", responseData.data.token)
            })
        } catch (_: JsonException) {
            unprocessedResponse()
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
        val accounts = mutableListOf<LocalAccount>()

        for (data in tokensFile.readText().split(";")) {
            try {
                accounts.add(
                    JsonIterator.deserialize(data, LocalAccount::class.java)
                        .apply { main = false })
            } catch (_: JsonException) {
                continue
            }
        }

        accounts.add(LocalAccount.constructLocalAccount(token, id))

        val writableAccounts = mutableListOf<String>()

        for (account in accounts) {
            if ((account.id == id) and !account.main) continue
            writableAccounts.add(JsonStream.serialize(account))
        }

        tokensFile.writeText(writableAccounts.joinToString(";"))
    }
}