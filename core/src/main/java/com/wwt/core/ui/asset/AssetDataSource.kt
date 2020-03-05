package com.wwt.core.ui.asset

import com.wwt.core.data.TeamMemberProfileModelDto

interface AssetDataSource {

    interface LoadWelcomeImageCallback {
        fun onImageListLoaded(images: MutableList<AssetImageModel>)
        fun onDataNotAvailable()
    }

    interface LoadTeamMembersProfileCallback {
        fun onProfilesListLoaded(teamMembersList: MutableList<TeamMemberProfileModelDto>)
        fun onDataNotAvailable(message: String)
    }

    fun getOfficeImageList(callback: LoadWelcomeImageCallback)
}