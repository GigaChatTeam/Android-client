package com.gct.cl.android

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
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
class ActivityRegistration : AppCompatActivity() {
    private val httpClient = HttpClient() {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
        }
    }

    private val path by lazy { if (DEBUG) getExternalFilesDir("") else filesDir }
    private val tokensFile by lazy { File(path, "localAccounts.sjson").apply { createNewFile() } }

    private lateinit var contact: TextInputEditText
    private lateinit var login: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var repeatPassword: TextInputEditText
    private lateinit var register: Button
    private lateinit var responseStatusRegistration: TextView
    private lateinit var showPassword: CheckBox

    private val specs = Regex("[^a-zA-Z0-9 ]")
    private val nums = Regex("\\d+")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        supportActionBar?.hide()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun binder() {
        contact = findViewById(R.id.contact)
        login = findViewById(R.id.loign)
        password = findViewById(R.id.password)
        repeatPassword = findViewById(R.id.repeatPassword)
        register = findViewById(R.id.register)
        showPassword = findViewById(R.id.showPassword)

        showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) password.setInputType(InputType.TYPE_CLASS_TEXT)
            else password.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            if (isChecked) repeatPassword.setInputType(InputType.TYPE_CLASS_TEXT)
            else repeatPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        }
    }

    private fun register() {
        responseStatusRegistration.setText("")

        if (checkContact() and checkPassword()) {
            GlobalScope.launch(Dispatchers.IO) {
                val response: HttpResponse =
                    httpClient.request(
                        URLS.REGISTRATION
                    ) {
                        method = HttpMethod.Post
                        url {
                            parameters.append(
                                URLS.REGISTRATION_USERNAME,
                                login.text.toString()
                            )
                            body = MultiPartFormDataContent(formData {
                                append(URLS.REGISTRATION_USERNAME, login)
                                append(URLS.REGISTRATION_CONTACT, contact)
                                append(URLS.REGISTRATION_PASSWORD, password.toString())
                            })
                        }
                    }
                Log.d("HTTP-STATUS", response.status.value.toString())
                Log.d("HTTP-RESPONSE", response.bodyAsText())

                when (response.status.value) {
                    200 -> completeRegistration(response.bodyAsText())
                    400 -> notValid(response.bodyAsText())
                    406 -> outdatedClient()
                    409 -> alreadyRegistered(response.bodyAsText())
                    429 -> tooManyRequests()
                    else -> unprocessedResponse()
                }

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun completeRegistration(responseText: String) {
        val responseData = JsonIterator.deserialize(
            responseText,
            ResponsePackets.AC.Registration.Done::class.java
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
    private fun alreadyRegistered(body: String) {
        val data = JsonIterator.deserialize(
            body,
            ResponsePackets.AC.Registration.AlreadyRegistered::class.java
        )
        runOnUiThread {
            responseStatusRegistration.text = when (data.target) {
                "name" ->
                    getString(R.string.registration_widgets_responseStatus_usernameAlreadyRegistered)
                "contact" ->
                    getString(R.string.registration_widgets_responseStatus_contactAlreadyRegistered)
                else -> getString(R.string.authorization_widgets_responseStatus_responseError)
            }
        }
    }

    private fun tooManyRequests() {
        runOnUiThread {
            responseStatusRegistration.text =
                getString(R.string.authorization_widgets_responseStatus_tooManyRequests)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun outdatedClient() {
        runOnUiThread {
            responseStatusRegistration.text =
                getString(R.string.authorization_widgets_responseStatus_outdatedClient)
        }
    }

    private fun notValid(body: String) { // 400
        val data =
            JsonIterator.deserialize(body, ResponsePackets.AC.Registration.NotValid::class.java)
        runOnUiThread {
            responseStatusRegistration.text = when (data.target) {
                "name" -> getString(R.string.registration_widgets_responseStatus_notValidName)

                "contact" -> getString(R.string.registration_widgets_responseStatus_notValidContact)

                "password" -> getString(R.string.registration_widgets_responseStatus_notValidPassword)

                else -> getString(R.string.authorization_widgets_responseStatus_responseError)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun unprocessedResponse() {
        runOnUiThread {
            responseStatusRegistration.text =
                getString(R.string.authorization_widgets_responseStatus_responseError)
        }
    }

    private fun saveToken(id: Long, token: String) {
        val accounts = mutableListOf<Helper.LocalAccount>()

        for (data in tokensFile.readText().split(";")) {
            try {
                accounts.add(
                    JsonIterator.deserialize(data, Helper.LocalAccount::class.java)
                        .apply { main = false })
            } catch (_: JsonException) {
                continue
            }

            Log.d(
                "TOKEN TO SAVE",
                JsonStream.serialize(
                    JsonIterator.deserialize(
                        data,
                        Helper.LocalAccount::class.java
                    )
                )
            )
        }

        accounts.add(Helper.constructLocalAccount(token, id))

        val writableAccounts = mutableListOf<String>()

        for (account in accounts) {
            writableAccounts.add(JsonStream.serialize(account))
        }

        tokensFile.writeText(writableAccounts.joinToString(";"))
    }

    private fun checkContact(): Boolean {
        return (contact.toString()[0].toString() == "+") xor (contact.toString().contains("@"))
    }

    private fun containsSpecialSymbolsAndNums(input: String): Boolean {
        return specs.containsMatchIn(input) and nums.containsMatchIn(input)
    }

    private fun checkPassword(): Boolean {
        return (password == repeatPassword) and (password.toString().length >= 8) and
                (containsSpecialSymbolsAndNums(password.toString()))
    }
}