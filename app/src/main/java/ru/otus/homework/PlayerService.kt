package ru.otus.homework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class PlayerService : LifecycleService() {
    // Messenger for receiving messages client -> server
    private lateinit var messenger: Messenger

    // Client messenger channel server -> client
    // Only one client is supported for simplicity
    private var client: WeakReference<Messenger>? = null

    private var job: Job? = null

    override fun onCreate() {
        Log.i(TAG, "Starting bound service")
        super.onCreate()
        messenger = Messenger(IncomingHandler(WeakReference(this)))
        startForeground()
        startPlayback()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Binding...")
        super.onBind(intent)
        return messenger.binder
    }

    override fun onDestroy() {
        Log.i(TAG, "Stopping location tracking service")
        job?.cancel()
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun startPlayback() {
        Log.i(TAG, "Starting playback...")
        job = lifecycleScope.launch {
            while (isActive) {
                lyrics.forEach {
                    sendMessage(it)
                    delay(300)
                }
            }
        }
    }

    // Send location to the client
    private fun sendMessage(text: String) {
        client?.get()?.send(
            Message.obtain(null, MSG_TEXT).apply {
                data = bundleOf(KEY_TEXT to text)
            }
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Unbinding...")
        client = null
        return false
    }

    private fun startForeground() {
        val channel = NotificationChannel(CHANNEL_ID, "Player", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Baby shark player"

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Player")
            .setContentText("Play baby shark song")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
    }

    // Handler of incoming messages from client
    // Client sends us registration and unregistration messages
    private class IncomingHandler(private val service: WeakReference<PlayerService>) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER -> {
                    service.get()?.client = WeakReference(msg.replyTo)
                }
                MSG_UNREGISTER -> {
                    service.get()?.client = null
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "player_channel"
        private const val TAG = "SomeBoundService"
        const val MSG_REGISTER = 1
        const val MSG_UNREGISTER = 2
        const val MSG_TEXT = 3
        const val KEY_TEXT = "text"

        private val lyrics = listOf(
            "Baby shark, doo doo doo doo doo doo",
            "Baby shark, doo doo doo doo doo doo",
            "Baby shark, doo doo doo doo doo doo",
            "Baby shark!"
        )
    }
}