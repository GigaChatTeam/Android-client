package com.gct.cl.android

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jsoniter.JsonIterator
import java.io.File


class Loader : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)
        val intents = Intent(this, ActivityAuthorization::class.java)
        startActivity(intents)
    }

    private fun tokenChecker(): Boolean {
        val token = File(filesDir, "example.com").readText()

        val tokens = JsonIterator.deserialize(token, HashMap::class.java)

        findViewById<TextView>(R.id.textView).setText(tokens.toString())

        return false
    }


}