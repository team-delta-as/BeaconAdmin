package com.wwt.core.filestore

interface ImageDownloadCallback {
    fun onImageSaved(uid: String)
    fun failure(message: String)
}