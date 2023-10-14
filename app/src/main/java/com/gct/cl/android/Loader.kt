package com.gct.cl.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File


class Loader : AppCompatActivity() {
    @Suppress("DEPRECATION")
    private val mainKeyAlias by lazy {
        val key = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        Log.i("MasterKet", key.toString())
    }
    @Suppress("DEPRECATION")
    private val tokensFile by lazy {
        EncryptedFile.Builder(
            File(filesDir, "tokens.json").apply { createNewFile() },
            application,
            mainKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)

        supportActionBar?.hide()

        reader()

        val intents = Intent(this, ActivityAuthorization::class.java)
        startActivity(intents)
    }

    private fun reader() {
        val text: String

        tokensFile.openFileInput().use {
            text = it.read().toString()
        }

        Log.d("TOKENS FILE", text)
    }
}
