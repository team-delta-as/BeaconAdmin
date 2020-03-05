package com.wwt.core.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import com.wwt.core.R

class StringProvider(private val context: Context) {
    fun emptyUsernameAndEmail(): String = getString(R.string.error_username_email)
    fun emptyUsername(): String = getString(R.string.error_username)
    fun emptyEmail(): String = getString(R.string.error_email)
    fun invalidEmail(): String = getString(R.string.error_email_validation)

    fun getString(@StringRes resId: Int): String = context.getString(resId)
    fun getStringWithValue(@StringRes resId: Int, value: String): String = context.getString(resId, value)
}

object StringProviderFactory {
    @SuppressLint("StaticFieldLeak")
    lateinit var stringProvider: StringProvider
}