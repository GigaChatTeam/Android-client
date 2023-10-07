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

        buttonLogIn.setOnClickListener {
            debugView.text = "Жопа Взломана"
            GlobalScope.launch(Dispatchers.IO) {
                val response: HttpResponse =
                    httpClient.request(
                        URLS.AUTHORIZATION
                    ) {
                        method = HttpMethod.Get
                        url {
                            parameters.append(URLS.AUTHORIZATION_USERNAME, inputLogin.text.toString())
                            parameters.append(URLS.AUTHORIZATION_PASSWORD, inputPassword.text.toString())
                        }
                    }
                debugView.text = "Done!"
                val data =
                    JsonIterator.deserialize(response.bodyAsText()).asMap()["auth-data"]!!.asMap()
                val token = data["token"]!!
                val id = data["id"]!!.toInt()
                debugView.text = "id: $id\ntoken: $token"
            }
        }

        showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputPassword.setInputType(InputType.TYPE_CLASS_TEXT)
            } else {
                inputPassword.setInputType(InputType.TYPE_CLASS_TEXT
                        or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
        }
    }
}