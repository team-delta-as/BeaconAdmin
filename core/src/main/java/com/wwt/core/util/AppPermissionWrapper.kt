package com.wwt.core.util

import android.content.Context

interface AppPermissionWrapper {
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
    fun isBluetoothValid(): Boolean
}

class AndroidAppPermissionWrapper(
    private val context: Context
) : AppPermissionWrapper {

    override fun hasLocationPermission(): Boolean = context.hasLocationPermission()

    override fun isLocationEnabled(): Boolean = context.isLocationEnabled()

    override fun isBluetoothValid(): Boolean =
        context.getBluetoothManager()?.adapter?.isEnabled ?: false
}

object AppPermissionWrapperFactory {
    lateinit var appPermissionWrapper: AppPermissionWrapper
}