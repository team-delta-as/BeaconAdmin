package com.wwt.core.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wwt.core.BaseViewModel
import com.wwt.core.Event

class AssetViewModel(
    private val assetRepository: AssetRepository = AssetRepository.getInstance()
) : BaseViewModel() {

    var userName = MutableLiveData<String>()
    private val _welcomeImageList = MutableLiveData<List<AssetImageModel>>().apply { value = emptyList() }
    val welcomeImageList: LiveData<List<AssetImageModel>>
        get() = _welcomeImageList

    private val _openImage = MutableLiveData<Event<AssetImageModel>>()
    val openImage: LiveData<Event<AssetImageModel>>
        get() = _openImage

    fun getWelcomeImagesList() {
        assetRepository.getOfficeImageList(object :
            AssetDataSource.LoadWelcomeImageCallback {
            override fun onImageListLoaded(images: MutableList<AssetImageModel>) {
                _welcomeImageList.value = images.toMutableList()
            }

            override fun onDataNotAvailable() {
                _welcomeImageList.value = emptyList()
            }
        })
    }

    fun openImage(assetImageModel: AssetImageModel) {
        _openImage.value = Event(assetImageModel)
    }
}