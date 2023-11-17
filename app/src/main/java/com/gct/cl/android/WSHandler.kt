package com.gct.cl.android

import android.util.Log
import com.jsoniter.output.JsonStream
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WSHandler(private val serverURI: String) : WebSocketClient(URI(serverURI)) {
    init {
        connect()
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("SERVER HANDSHAKE", handshakedata?.toString() ?: "NULL POINT")
    }

    override fun onMessage(message: String?) {
        Log.d("MESSAGE", message ?: "NULL POINT")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("CLOSE CODE", code.toString())
        Log.d("REASON", reason ?: "NULL POINT")
        Log.d("REMOTE", remote.toString())
    }

    override fun onError(ex: Exception?) {
        Log.d("ERROR", ex?.stackTraceToString() ?: "NULL POINT")
    }

    fun send(intentions: ArrayList<String>, data: Any) {
        val serialized = JsonStream.serialize(data)

        send("${intentions.joinToString("-")}%${Helper.SHA512(serialized)}%${serialized}")
    }
}
