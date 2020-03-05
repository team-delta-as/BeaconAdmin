package com.wwt.core.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.wwt.core.R
import com.wwt.core.data.TeamMemberProfileModelDto
import com.wwt.core.ui.asset.AssetImageModel

@BindingAdapter("android:src")
fun setImageUrl(imageView: ImageView, assetImageModel: AssetImageModel) {
    imageView.loadImage(
        imageView.context.getString(
            R.string.asset_image_path,
            assetImageModel.assetDirName,
            assetImageModel.imageSource
        ), placeholder = -1
    )
}

@BindingAdapter("android:src")
fun setImageUrl(imageView: ImageView, imageUrl: String) {
    imageView.loadImage(imageUrl, placeholder = -1)
}

@BindingAdapter("android:srcForNullable")
fun setImageUrlForNullableString(imageView: ImageView, imageUrl: String?) {
    imageUrl?.let {
        imageView.loadImage(imageUrl, placeholder = -1)
    }
}

@BindingAdapter("android:src")
fun setImageUrl(imageView: ImageView, teamMemberProfileModel: TeamMemberProfileModelDto?) {
    teamMemberProfileModel?.let {
        imageView.loadImageWithRotate(
            teamMemberProfileModel.storageUri, placeholder = R.drawable.ic_image_placeholder
        )
    }
}