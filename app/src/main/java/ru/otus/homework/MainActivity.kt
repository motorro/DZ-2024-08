package ru.otus.homework

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
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
        setupAutocomplete()
        setupRecyclerView()
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

    companion object {
        private const val TAG = "MainActivity"
    }
}