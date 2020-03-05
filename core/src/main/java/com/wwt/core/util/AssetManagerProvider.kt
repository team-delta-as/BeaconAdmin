package com.wwt.core.util

import android.annotation.SuppressLint
import android.content.Context
import com.wwt.core.ui.asset.AssetImageModel

class AssetManagerProvider(private val context: Context) {
    private fun getImageList(assetDirName: String): Array<String>? = context.assets.list(assetDirName)

    fun getAssetImageList(assetDirName: String): MutableList<AssetImageModel>? {
        var assetImageList: MutableList<AssetImageModel>? = null
        val imageList = getImageList(assetDirName)
        imageList?.let { images ->
            assetImageList = mutableListOf()
            images.forEach {
                assetImageList?.add(AssetImageModel(it, assetDirName))
            }
        }
        return assetImageList
    }
}

object AssetManagerFactory {
    @SuppressLint("StaticFieldLeak")
    lateinit var assetManagerProvider: AssetManagerProvider
}