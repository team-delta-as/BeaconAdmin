package com.wwt.core.filestore

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserInfo(
    var user_id: String? = null,
    var app_version: String? = null,
    var device_info: DeviceInfo? = null,
    var personal_info: PersonalInfo? = null,
    var timeStamp: String? = null,
    @JvmField var is_team_member: Boolean = false,
    var favorites: FavoritesInfo? = null
): Parcelable {
    companion object
}

@Keep
@Parcelize
data class DeviceInfo(
    var os_type: String? = OS_TYPE,
    var os_version: String? = null
): Parcelable {
    companion object {
        const val OS_TYPE = "ANDROID"
    }
}

@Keep
@Parcelize
data class PersonalInfo(
    var name: String? = null,
    var email: String? = null,
    var email_primarykey: String? = null,
    var preferred_name: String? = null,
    var image_url: String? = null
): Parcelable {
    companion object
}

@Keep
@Parcelize
data class FavoritesInfo(
    var hyderabad: String? = null,
    var denver: String? = null
): Parcelable {
    companion object
}