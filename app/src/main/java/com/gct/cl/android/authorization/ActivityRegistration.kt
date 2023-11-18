package com.gct.cl.android.authorization

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
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


@OptIn(InternalAPI::class)
class ActivityRegistration : AppCompatActivity() {
    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
        }
    }

    private var blocked = false

    private val path by lazy { if (DEBUG) getExternalFilesDir("") else filesDir }
    private val tokensFile by lazy { File(path, "localAccounts.sjson").apply { createNewFile() } }

    private lateinit var contact: TextInputEditText
    private lateinit var login: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var repeatPassword: TextInputEditText
    private lateinit var register: Button
    private lateinit var responseStatusRegistration: TextView
    private lateinit var showPassword: CheckBox

    private val specs = Regex("[^a-zA-Z0-9]")
    private val nums = Regex("\\d+")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        supportActionBar?.hide()

        binder()
    }

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

    @OptIn(DelicateCoroutinesApi::class)
    private fun register() {
        if (blocked) {
            tooManyRequests()
            return
        }

        blocked = true


        responseStatusRegistration.text = ""

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

                when (response.status.value) {
                    200 -> completeRegistration(response.bodyAsText())
                    400 -> notValid(response.bodyAsText())
                    406 -> outdatedClient()
                    409 -> alreadyRegistered(response.bodyAsText())
                    429 -> tooManyRequests()
                    else -> unprocessedResponse()
                }

                blocked = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun completeRegistration(responseText: String) {
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
    private fun alreadyRegistered(body: String) {
        val data = JsonIterator.deserialize(
            body,
            Packets.Registration.AlreadyRegistered::class.java
        )
        runOnUiThread {
            responseStatusRegistration.text = when (data.target) {
                "name" -> getString(R.string.registration_widgets_responseStatus_usernameAlreadyRegistered)
                "contact" -> getString(R.string.registration_widgets_responseStatus_contactAlreadyRegistered)
                else -> getString(R.string.authorization_widgets_responseStatus_responseError)
            }
        }
    }

    @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
    private fun notValid(body: String) { // 400
        val data =
            JsonIterator.deserialize(body, Packets.Registration.NotValid::class.java)
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