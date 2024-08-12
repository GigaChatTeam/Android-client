package com.gigachat.product

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

val globalHTTPClient = HttpClient(Android) {

}

val globalCoroutineScope = CoroutineScope(Dispatchers.Default + CoroutineName("Global scope"))
