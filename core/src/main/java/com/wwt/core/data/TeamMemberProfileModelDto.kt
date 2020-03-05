package com.wwt.core.data

import android.os.Parcel
import android.os.Parcelable

data class TeamMemberProfileModelDto(
    val documentId: String,
    val url: String?,
    val teamMemberName: String,
    val teamMemberNickname: String,
    val isTeamMember: Int,
    var storageUri: String = ""
) : Parcelable {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(documentId)
        parcel.writeString(url)
        parcel.writeString(teamMemberName)
        parcel.writeString(teamMemberNickname)
        parcel.writeInt(isTeamMember)
        parcel.writeString(storageUri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TeamMemberProfileModelDto> {
        override fun createFromParcel(parcel: Parcel): TeamMemberProfileModelDto {
            return TeamMemberProfileModelDto(parcel)
        }

        override fun newArray(size: Int): Array<TeamMemberProfileModelDto?> {
            return arrayOfNulls(size)
        }
    }
}