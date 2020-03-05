package com.wwt.core.util

import android.annotation.SuppressLint
import android.app.Activity
import android.text.SpannableStringBuilder
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.wwt.core.R

@SuppressLint("NewApi")
fun Activity.appSnackbar(coordinatorLayout: CoordinatorLayout, message: String, duration: Int) =
    Snackbar.make(
        coordinatorLayout,
        SpannableStringBuilder(message).setColor(this, android.R.color.white),
        duration
    ).apply {
        view.findViewById<TextView>(R.id.snackbar_text).typeface = resources.getFont(R.font.app_roboto_regular)
        view.setBackgroundColor(ContextCompat.getColor(this@appSnackbar, R.color.dark_transparent_80))
        setActionTextColor(ContextCompat.getColor(view.context, android.R.color.white))
        view.findViewById<TextView>(R.id.snackbar_action).typeface = resources.getFont(R.font.app_roboto_regular)
    }