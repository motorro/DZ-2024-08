package ru.otus.homework

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import kotlinx.coroutines.launch
import ru.otus.homework.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Messenger for sending messages to the bound service
    private var messenger: WeakReference<Messenger>? = null
    // Messenger for receiving messages from the service
    private val incomingMessenger = Messenger(IncomingHandler(WeakReference(this)))
    // Flag indicating whether we have called bind on the service
    private var bound: Boolean = false

    // Service connection for binding to the service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bound = true
            // Register message channel client -> service
            messenger = WeakReference(Messenger(service))
            val message = Message.obtain(null, PlayerService.MSG_REGISTER).apply {
                // Set the reply messenger to receive messages server -> client
                replyTo = incomingMessenger
            }
            // Register client
            messenger?.get()?.send(message)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            messenger = null
        }
    }

    // Handler of incoming messages from service
    // Server sends us location updates
    private class IncomingHandler(private val activity: WeakReference<MainActivity>) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                PlayerService.MSG_TEXT -> {
                    val text = msg.data.getString(PlayerService.KEY_TEXT) ?: "No lyrics!"
                    Log.i(TAG, text)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            buttonStatic.setOnClickListener { v -> showStaticMenu(v, R.menu.menu_static) }
            buttonDynamic.setOnClickListener { v -> showDynamicMenu(v, R.menu.menu_static) }
            buttonOperation.setOnClickListener {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        someOperation()
                    }
                }
            }
            buttonBgService.setOnClickListener {
                startService(SomeService.createIntent(this@MainActivity))
            }
            buttonBoundService.setOnClickListener {
                if (bound) {
                    enough()
                    buttonBoundService.text = getString(R.string.btn_listen)
                } else {
                    listen()
                    buttonBoundService.text = getString(R.string.btn_enough)
                }
            }
        }
        setupAutocomplete()
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        checkNotificationPermission()
    }

    private fun showStaticMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            Log.i(TAG, "Menu item clicked: ${menuItem.title}")
            true
        }
        popup.setOnDismissListener {
            Log.i(TAG, "Popup menu dismissed")
        }
        // Show the popup menu.
        popup.show()
    }

    private fun showDynamicMenu(v: View, @MenuRes menuRes: Int) {
        val listPopupWindow = ListPopupWindow(
            this,
            null,
            androidx.appcompat.R.attr.listPopupWindowStyle
        )
        listPopupWindow.anchorView = v

        val items = listOf(
            getString(R.string.option_1),
            getString(R.string.option_2),
            getString(R.string.option_3)
        )
        val adapter = ArrayAdapter(this, R.layout.item_menu, items)
        listPopupWindow.setAdapter(adapter)

        listPopupWindow.setOnItemClickListener { _, _, position: Int, _ ->
            Log.i(TAG, "Menu item clicked: ${items[position]}")
            listPopupWindow.dismiss()
        }
        // Show the popup menu.
        listPopupWindow.show()
    }

    private fun setupAutocomplete() {
        val items = listOf(
            getString(R.string.option_1),
            getString(R.string.option_2),
            getString(R.string.option_3)
        )
        val adapter = ArrayAdapter(this, R.layout.item_menu, items)
        binding.textInputEditText.setAdapter(adapter)
    }

    private fun setupRecyclerView() {
        val adapter = SomeItemAdapter()
        val dashLine = requireNotNull(AppCompatResources.getDrawable(
            this@MainActivity,
            R.drawable.divider)
        )
        val decoration = DividerItemDecoration(this@MainActivity, VERTICAL).apply {
            setDrawable(dashLine)
        }
        binding.recyclerView.let {
            it.setHasFixedSize(true)
            it.adapter = adapter
            it.addItemDecoration(decoration)
        }
        adapter.submitList(
            listOf(
                SomeItem("Item 1"),
                SomeItem("Item 2"),
                SomeItem("Item 3")
            )
        )
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS).let { result ->
                val granted = PackageManager.PERMISSION_GRANTED == result
                if (granted) {
                    startService()
                } else {
                    notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
                }
            }
        } else {
            startService()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        startService()
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG,"Activity stopped!")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
        Log.i(TAG,"Activity destroyed!")
    }

    // Start location tracking service which will run in the foreground
    private fun startService() {
        Log.i(TAG, "Starting player service")
        val intent = Intent(this, PlayerService::class.java)
        startForegroundService(intent)
    }

    // Stop location tracking service
    private fun stopService() {
        Log.i(TAG, "Stopping player service")
        enough()
        val intent = Intent(this, PlayerService::class.java)
        stopService(intent)
    }

    // Listen to the music
    private fun listen() {
        Log.i(TAG, "Listening...")
        if (bound) {
            return
        }
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Stop listening to the music
    private fun enough() {
        Log.i(TAG, "Stopping listening...")
        if (bound.not()) {
            return
        }
        val message = Message.obtain(null, PlayerService.MSG_UNREGISTER)
        messenger?.get()?.send(message)

        unbindService(connection)
        bound = false
        messenger = null
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}