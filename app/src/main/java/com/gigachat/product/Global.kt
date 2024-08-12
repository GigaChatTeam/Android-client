package com.gigachat.product

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

val globalHTTPClient = HttpClient(Android) {

}
