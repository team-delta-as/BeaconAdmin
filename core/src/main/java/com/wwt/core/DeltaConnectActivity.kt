package com.wwt.core

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.wwt.core.util.appSnackbar
import java.io.Closeable

abstract class DeltaConnectActivity : BaseActivity() {

    private var menuSelectionCallback: (MenuItem) -> Unit? = {}
    private var menuLayout: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        log("onCreate hasSavedInstanceState=${savedInstanceState != null}")
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        log("onPostCreate hasSavedInstanceState=${savedInstanceState != null}")
        super.onPostCreate(savedInstanceState)
    }

    override fun onStart() {
        log("onStart")
        super.onStart()
    }

    override fun onResume() {
        log("onResume")
        super.onResume()
    }

    override fun onPause() {
        log("onPause isFinishing=$isFinishing")
        super.onPause()
    }

    override fun onStop() {
        log("onStop isFinishing=$isFinishing")
        super.onStop()
    }

    override fun onDetachedFromWindow() {
        log("onDetachedFromWindow isFinishing=$isFinishing")
        super.onDetachedFromWindow()
    }

    override fun onDestroy() {
        log("onDestroy isFinishing=$isFinishing")
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        log("onNewIntent")
        super.onNewIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onUpPressed()
                return true
            }
            else -> {
                menuSelectionCallback.invoke(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    open fun onUpPressed() = onBackPressed()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuLayout?.let {
            menuInflater.inflate(it, menu)
        }
        return true
    }

    private fun setCustomActionBar(
        toolbar: Toolbar,
        displayUp: Boolean,
        menuSelectionCallback: (MenuItem) -> Unit,
        menuLayout: Int?
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(displayUp)
        this.menuSelectionCallback = menuSelectionCallback
        this.menuLayout = menuLayout
        invalidateOptionsMenu()
    }

    fun setActionBar(
        title: Int,
        toolbar: Toolbar,
        displayUp: Boolean = true,
        menuSelectionCallback: (MenuItem) -> Unit = {},
        menuLayout: Int? = null
    ) {
        toolbar.title = getString(title)
        setCustomActionBar(toolbar, displayUp, menuSelectionCallback, menuLayout)
    }

    fun setActionBar(
        title: String,
        toolbar: Toolbar,
        displayUp: Boolean = true,
        menuSelectionCallback: (MenuItem) -> Unit = {},
        menuLayout: Int? = null
    ) {
        toolbar.title = title
        setCustomActionBar(toolbar, displayUp, menuSelectionCallback, menuLayout)
    }

    private fun log(message: String) {
        Log.d(TAG, "${this.javaClass.simpleName} ${hashCode()} $message")
    }

    fun showFailure(
        message: String,
        coordinatorLayout: CoordinatorLayout?,
        retryCallback: ((View) -> Unit)? = null,
        duration: Int = Snackbar.LENGTH_INDEFINITE
    ): Closeable {
        coordinatorLayout?.let {
            val snackbar = appSnackbar(coordinatorLayout, message, duration).apply {
                retryCallback?.let { setAction(getString(R.string.retry), retryCallback) }
                show()
            }
            return Closeable { snackbar.dismiss() }
        }
        return Closeable { }
    }

    abstract val coordinatorLayoutForSnackbar: CoordinatorLayout?

    fun showBriefMessage(message: String) {
        coordinatorLayoutForSnackbar?.let { appSnackbar(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    fun showToastMessage(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showToastMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = DeltaConnectActivity::class.java.simpleName
    }
}