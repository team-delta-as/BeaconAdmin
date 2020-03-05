package com.wwt.core.compass

import android.content.Context

interface CompassFeature {
    fun showNavigationScreen(launchedFromNotification: Boolean)
    fun showPrePermissionScreen()

    interface Provider {
        fun get(dependencies: Dependencies): CompassFeature
    }

    interface Dependencies {
        fun getContext(): Context
    }
}