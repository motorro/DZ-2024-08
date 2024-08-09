package ru.otus.homework

import android.util.Log
import kotlinx.coroutines.delay

suspend fun someOperation(count: Int = 10) {
    repeat(count) {
        Log.i(TAG, "Count ${it + 1}")
        delay(1000L)
    }
}

const val TAG = "SomeTag"