package com.gct.cl.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class Loader : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)

        supportActionBar?.hide()

        val intents = Intent(this, ActivityAuthorization::class.java)
        startActivity(intents)
    }
}