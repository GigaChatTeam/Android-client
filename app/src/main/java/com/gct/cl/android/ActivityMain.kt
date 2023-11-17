package com.gct.cl.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ActivityMain : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
    }
}