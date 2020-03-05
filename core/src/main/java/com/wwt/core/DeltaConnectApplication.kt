package com.wwt.core

import android.app.Application
import com.wwt.core.util.*

class DeltaConnectApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AssetManagerFactory.assetManagerProvider = AssetManagerProvider(this)
        SharedPreferenceFactory.sharedPreferenceProvider =
            SharedPreferenceProvider(this)
        AppPermissionWrapperFactory.appPermissionWrapper = AndroidAppPermissionWrapper(this)
    }
}