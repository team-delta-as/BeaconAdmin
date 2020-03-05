package com.wwt.core.util

import android.Manifest
import androidx.fragment.app.Fragment

fun Fragment.arguments() = arguments!!

fun Fragment.requestLocationPermissions(requestCode: Int) =
    this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)