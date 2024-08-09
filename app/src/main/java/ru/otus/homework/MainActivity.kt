package ru.otus.homework

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.otus.homework.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            buttonStatic.setOnClickListener { v -> showStaticMenu(v, R.menu.menu_static) }
        }
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

    companion object {
        private const val TAG = "MainActivity"
    }
}