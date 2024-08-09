package ru.otus.homework

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import ru.otus.homework.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            buttonStatic.setOnClickListener { v -> showStaticMenu(v, R.menu.menu_static) }
            buttonDynamic.setOnClickListener { v -> showDynamicMenu(v, R.menu.menu_static) }
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

    companion object {
        private const val TAG = "MainActivity"
    }
}