package com.wwt.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wwt.core.filestore.UserInfo

class BroadcastProvider(
    context: Context,
    private val intentFactory: IntentFactory = IntentFactory(),
    private val localBroadcastManager: LocalBroadcastManager = LocalBroadcastManager.getInstance(context)
) {

    fun updateUserName() {
        localBroadcastManager.sendBroadcast(intentFactory.create().apply {
            action = UPDATE_USERNAME_ACTION
        })
    }

    fun updateTeamInfo() {
        localBroadcastManager.sendBroadcast(intentFactory.create().apply {
            action = UPDATE_TEAM_MEMBER_INFO_ACTION
        })
    }

    fun updatePoiList(data: UserInfo) {
        localBroadcastManager.sendBroadcast(intentFactory.create().apply {
            action = UPDATE_POI_LIST_ACTION
            putExtra(MODIFIED_DOCUMENT, data)
        })
    }

    companion object {
        private const val UPDATE_USERNAME_ACTION = "updateUsernameAction"
        private const val UPDATE_TEAM_MEMBER_INFO_ACTION = "updateTeamInfoAction"
        private const val UPDATE_POI_LIST_ACTION = "updatePoiListAction"
        private const val MODIFIED_DOCUMENT = "modifiedDocument"

        fun intentFilterForUpdateUsername(): IntentFilter {
            return IntentFilter(UPDATE_USERNAME_ACTION)
        }

        fun intentFilterForUpdateTeamInfo(): IntentFilter {
            return IntentFilter(UPDATE_TEAM_MEMBER_INFO_ACTION)
        }

        fun intentFilterForUpdatePoiList(): IntentFilter {
            return IntentFilter(UPDATE_POI_LIST_ACTION)
        }
    }
}

class IntentFactory {
    fun create() = Intent()
}

object BroadcastProviderFactory {
    @SuppressLint("StaticFieldLeak")
    lateinit var broadcastProvider: BroadcastProvider
}