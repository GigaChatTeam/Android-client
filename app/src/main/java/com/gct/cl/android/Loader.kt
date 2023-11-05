package com.gct.cl.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import com.jsoniter.spi.JsonException
import java.io.File


class Loader : AppCompatActivity() {
    private val path by lazy { if (DEBUG) getExternalFilesDir("") else filesDir }
    private val tokensFile by lazy { File(path, "localAccounts.sjson").apply { createNewFile() } }

    private var account: Helper.LocalAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)

        supportActionBar?.hide()

        account = reader()

        if (account == null) {
            startActivity(Intent(this, ActivityAuthorization::class.java))
        }
        else {
            startActivity(Intent(this, ActivityMain::class.java).apply {
                putExtra("token", account!!.token)
                putExtra("id", account!!.id)
            })
        }
    }

    private fun reader(): Helper.LocalAccount? {
        for (data in tokensFile.readText().split(";")) {
            var account: Helper.LocalAccount

            try {
                account = JsonIterator.deserialize(data, Helper.LocalAccount::class.java)
            }
            catch (_: JsonException) {
                continue
            }

            Log.d("TOKEN FROM TOKENS FILE", JsonStream.serialize(account))
            if (account?.main == true) return account
        }

        return null
    }
}
