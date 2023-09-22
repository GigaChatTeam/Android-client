package com.gct.cl.android

import android.app.Service
import android.content.Intent
import android.os.IBinder

@Suppress("UNREACHABLE_CODE")
class WSHandler :  Service() {
    override fun onBind(intent: Intent): IBinder {
        if (intent.getBooleanExtra("isReconnecting", true)) {
            TODO()
        }
        return TODO("Provide the return value")
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (flags and START_FLAG_RETRY === 0) {
            TODO()
        } else {
            TODO()
        }
        return START_STICKY
    }
}