package com.wwt.core.network

import android.annotation.SuppressLint
import android.content.Context
import com.wwt.core.util.getConnectivityManager

interface NetworkInfoProviderWrapper {
    fun isConnected(): Boolean
}

class NetworkInfoProvider(
        private val context: Context
) : NetworkInfoProviderWrapper {

    override fun isConnected(): Boolean =
            context.getConnectivityManager()?.activeNetworkInfo?.isConnected ?: false
}

object NetworkInfoProviderFactory {
    @SuppressLint("StaticFieldLeak")
    lateinit var networkInfoProvider: NetworkInfoProviderWrapper
}