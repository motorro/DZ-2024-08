package ru.otus.homework

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SomeService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service created")
        lifecycleScope.launch {
            someOperation()
            Log.i(TAG, "Service operation completed")
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        private const val TAG = "SomeService"

        fun createIntent(context: MainActivity): Intent {
            return Intent(context, SomeService::class.java)
        }
    }
}

