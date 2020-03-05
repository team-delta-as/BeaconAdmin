package com.wwt.core.ui.asset

import com.wwt.core.util.AssetManagerFactory
import com.wwt.core.util.AssetManagerProvider

class AssetRepository(
    private val assetManagerProvider: AssetManagerProvider
) : AssetDataSource {

    override fun getOfficeImageList(callback: AssetDataSource.LoadWelcomeImageCallback) {
        val imageList = assetManagerProvider.getAssetImageList(ASSET_WELCOME_IMAGE_PATH)
        imageList?.let {
            if (imageList.isNotEmpty()) {
                callback.onImageListLoaded(imageList)
            } else {
                callback.onDataNotAvailable()
            }
        }
    }

    companion object {
        private var INSTANCE: AssetRepository? = null
        fun getInstance(
            assetManagerProvider: AssetManagerProvider = AssetManagerFactory.assetManagerProvider
        ) =
            INSTANCE
                ?: synchronized(AssetRepository::class.java) {
                    INSTANCE
                        ?: AssetRepository(assetManagerProvider).also { INSTANCE = it }
                }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }

        private const val ASSET_WELCOME_IMAGE_PATH = "welcome_images"
    }
}
