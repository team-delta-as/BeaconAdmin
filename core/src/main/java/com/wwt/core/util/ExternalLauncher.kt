package com.wwt.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent

interface ExternalLauncher {
    fun launch(context: Context, intent: Intent)
    fun externalAppExistsForIntent(context: Context, intent: Intent): Boolean
    fun launchForResult(activity: Activity, intent: Intent, requestCode: Int)
}

class AndroidExternalLauncher : ExternalLauncher {

    override fun launch(context: Context, intent: Intent) {
        context.startActivity(intent)
    }

    override fun externalAppExistsForIntent(context: Context, intent: Intent): Boolean =
        context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()

    override fun launchForResult(activity: Activity, intent: Intent, requestCode: Int) {
        activity.startActivityForResult(intent, requestCode)
    }
}

object ExternalLauncherFactory {
    lateinit var externalLauncher: ExternalLauncher
}