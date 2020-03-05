package com.wwt.core.util

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker

fun Context.getBluetoothManager() = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
fun Context.getConnectivityManager() = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

fun Context.hasLocationPermission() =
    ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PermissionChecker.PERMISSION_GRANTED

fun Context.isLocationEnabled(): Boolean {
    val locationMode: Int
    try {
        locationMode = Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE)
    } catch (e: SettingNotFoundException) {
        return false
    }
    return locationMode != Settings.Secure.LOCATION_MODE_OFF
}

fun Context.getLocationManager() = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager