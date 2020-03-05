package com.wwt.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    private val isLoadingIndicatorVisible = MutableLiveData<Boolean>()

    protected open fun onViewAttached(firstAttach: Boolean) {}

    protected open fun onViewDetached() {}

    fun attachView(firstAttach: Boolean) {
        onViewAttached(firstAttach)
    }

    val setLoadingIndicatorVisibility: LiveData<Boolean>
        get() = isLoadingIndicatorVisible

    fun showLoadingIndicator() {
        isLoadingIndicatorVisible.postValue(true)
    }

    fun hideLoadingIndicator() {
        isLoadingIndicatorVisible.postValue(false)
    }

    open fun refresh() {}

    fun detachView() {
        onViewDetached()
    }
}