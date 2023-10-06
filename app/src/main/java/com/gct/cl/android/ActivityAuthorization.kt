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
    val httpClient = HttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        supportActionBar?.hide()

        binder()
    }

    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    private fun binder() {
        val text = findViewById<TextView>(R.id.textView2)
        val login = findViewById<TextInputEditText>(R.id.textInputEditText)
        val pass = findViewById<TextInputEditText>(R.id.textInputEditText2)
        val button = findViewById<Button>(R.id.button)

        val showPassword = findViewById<CheckBox>(R.id.activityAuthorization_widgets_showPassword)

        button.setOnClickListener {
            text.text = "Жопа Взломана"
            GlobalScope.launch(Dispatchers.IO) {
                val response: HttpResponse =
                    httpClient.request(
                        "http://192.168.196.60:8082/auth"
                    ) {
                        method = HttpMethod.Get
                        url {
                            parameters.append("username", login.text.toString())
                            parameters.append("password", pass.text.toString())
                        }
                    }
                text.text = "Done!"
                val data =
                    JsonIterator.deserialize(response.bodyAsText()).asMap().get("auth-data")!!.asMap()
                val token = data.get("token")!!
                val id = data.get("id")!!.toInt()
                text.text = "token: $token\nid: $id"
            }
        }

        showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                pass.setInputType(InputType.TYPE_CLASS_TEXT)
            } else {
                pass.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
        }
    }
}