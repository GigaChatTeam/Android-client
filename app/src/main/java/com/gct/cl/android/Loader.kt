package com.gct.cl.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Loader : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)

        supportActionBar?.hide()
    }
}